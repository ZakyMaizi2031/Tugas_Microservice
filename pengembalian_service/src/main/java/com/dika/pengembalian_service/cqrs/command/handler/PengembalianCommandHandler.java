package com.dika.pengembalian_service.cqrs.command.handler;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.dika.pengembalian_service.cqrs.command.model.Pengembalian;
import com.dika.pengembalian_service.dto.PengembalianEvent;
import com.dika.pengembalian_service.repository.PengembalianJpaRepository;
import com.dika.pengembalian_service.service.EmailDendaService;
import com.dika.pengembalian_service.vo.Anggota;
import com.dika.pengembalian_service.vo.Buku;
import com.dika.pengembalian_service.vo.Peminjaman;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j // Aktifkan logging profesional
public class PengembalianCommandHandler {

    @Autowired private PengembalianJpaRepository jpaRepo;
    @Autowired private RabbitTemplate rabbitTemplate;
    @Autowired private RestTemplate restTemplate;
    @Autowired private EmailDendaService emailDendaService;

    @Value("${service.peminjaman.url:http://localhost:8083}")
    private String peminjamanServiceUrl;

    @Value("${service.anggota.url:http://localhost:8082}")
    private String anggotaServiceUrl;

    @Value("${service.buku.url:http://localhost:8081}")
    private String bukuServiceUrl;

    // --- 1. CREATE (PROSES PENGEMBALIAN) ---
    @Transactional
    public Pengembalian handleCreate(Pengembalian p) {
        log.info("[COMMAND] Memproses pengembalian baru untuk Peminjaman ID: {}", p.getPeminjamanId());

        // A. Ambil Data Peminjaman (VO)
        log.info("[EXTERNAL] Mengambil data Peminjaman dari: {}", peminjamanServiceUrl);
        Peminjaman peminjaman = restTemplate.getForObject(
                peminjamanServiceUrl + "/api/peminjaman/" + p.getPeminjamanId(), 
                Peminjaman.class);
        
        if (peminjaman == null) {
            log.error("[COMMAND_ERROR] Data Peminjaman ID {} tidak ditemukan!", p.getPeminjamanId());
            throw new RuntimeException("Gagal memproses: Data Peminjaman tidak ditemukan");
        }

        // B. Hitung Denda
        p = hitungDendaLogic(p, peminjaman.getTanggalPinjam());
        
        // C. Simpan ke MySQL
        Pengembalian saved = jpaRepo.save(p);
        log.info("[COMMAND] MySQL: Data Pengembalian ID {} berhasil disimpan. Denda: Rp {}", saved.getId(), saved.getDenda());

        // D. Kirim Email jika ada denda
        if (saved.getDenda() > 0) {
            log.info("[NOTIFICATION] Terdeteksi denda. Menyiapkan pengiriman email...");
            kirimEmailDenda(saved, peminjaman);
        }

        // E. SINKRONISASI INTERNAL (MySQL -> MongoDB Pengembalian)
        sendSyncMessage(saved);

        // F. BROADCAST KE SERVICE LAIN (Update Buku & Peminjaman)
        log.info("[EXTERNAL] Mengirim sinyal update ke Service Buku & Service Peminjaman");
        broadcastToOtherServices(saved, peminjaman, "Tersedia");

        return saved;
    }

    // --- 2. UPDATE ---
    @Transactional
    public Pengembalian handleUpdate(Long id, Pengembalian pReq) {
        log.info("[COMMAND] Memproses update data Pengembalian ID: {}", id);

        return jpaRepo.findById(id).map(p -> {
            p.setTanggal_dikembalikan(pReq.getTanggal_dikembalikan());
            
            Peminjaman peminjaman = restTemplate.getForObject(
                    peminjamanServiceUrl + "/api/peminjaman/" + p.getPeminjamanId(), 
                    Peminjaman.class);
            
            Pengembalian updated = jpaRepo.save(hitungDendaLogic(p, peminjaman.getTanggalPinjam()));
            log.info("[COMMAND] MySQL Update Sukses untuk ID: {}", id);
            
            sendSyncMessage(updated); 
            return updated;
        }).orElseThrow(() -> {
            log.error("[COMMAND_ERROR] Data Pengembalian ID {} tidak ditemukan", id);
            return new RuntimeException("Data tidak ditemukan");
        });
    }

    // --- 3. DELETE ---
    @Transactional
    public void handleDelete(Long id) {
        log.info("[COMMAND] Memproses penghapusan data Pengembalian ID: {}", id);

        if (jpaRepo.existsById(id)) {
            jpaRepo.deleteById(id);
            log.info("[COMMAND] MySQL: Data ID {} berhasil dihapus", id);

            log.info("[SYNC] Mengirim sinyal hapus ke RabbitMQ untuk MongoDB");
            rabbitTemplate.convertAndSend("x.perpustakaan", "pengembalian.delete", id);
        } else {
            log.warn("[COMMAND_WARN] Gagal hapus: ID {} tidak ditemukan", id);
        }
    }

    // --- HELPER METHODS DENGAN LOG ---

    private Pengembalian hitungDendaLogic(Pengembalian p, String tglPinjamStr) {
        LocalDate tglPinjam = LocalDate.parse(tglPinjamStr);
        LocalDate tglKembali = LocalDate.parse(p.getTanggal_dikembalikan());
        long selisih = ChronoUnit.DAYS.between(tglPinjam, tglKembali);
        
        long terlambat = selisih > 7 ? selisih - 7 : 0;
        p.setTerlambat(String.valueOf(terlambat));
        p.setDenda(terlambat * 2000);
        
        log.info("[LOGIC] Perhitungan denda: Terlambat {} hari", terlambat);
        return p;
    }

    private void sendSyncMessage(Pengembalian p) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", p.getId());
        data.put("peminjamanId", p.getPeminjamanId());
        data.put("tanggal_dikembalikan", p.getTanggal_dikembalikan());
        data.put("terlambat", p.getTerlambat());
        data.put("denda", p.getDenda());
        
        rabbitTemplate.convertAndSend("x.perpustakaan", "pengembalian.sync", data);
        log.info("[SYNC] Data dikirim ke RabbitMQ untuk sinkronisasi MongoDB");
    }

    private void broadcastToOtherServices(Pengembalian p, Peminjaman pem, String statusBuku) {
        PengembalianEvent event = new PengembalianEvent();
        event.setIdPengembalian(p.getId());
        event.setTanggalDikembalikan(p.getTanggal_dikembalikan());
        event.setDenda(p.getDenda());
        event.setIdPeminjaman(p.getPeminjamanId());
        event.setIdBuku(pem.getBukuId());
        event.setIdAnggota(pem.getAnggotaId());
        event.setStatus(statusBuku);

        rabbitTemplate.convertAndSend("x.perpustakaan", "buku.update.status", event);
        rabbitTemplate.convertAndSend("x.perpustakaan", "pengembalian.sukses", event);
    }

    private void kirimEmailDenda(Pengembalian p, Peminjaman pem) {
        try {
            log.info("[EXTERNAL] Mengambil data Anggota & Buku untuk notifikasi email...");
            Anggota anggota = restTemplate.getForObject(anggotaServiceUrl + "/api/anggota/" + pem.getAnggotaId(), Anggota.class);
            Buku buku = restTemplate.getForObject(bukuServiceUrl + "/api/buku/" + pem.getBukuId(), Buku.class);
            
            if (anggota != null && buku != null) {
                emailDendaService.kirimEmailDenda(
                    anggota.getEmail(), 
                    anggota.getNama(), 
                    buku.getJudul(), 
                    Long.parseLong(p.getTerlambat()), 
                    p.getDenda()
                );
                log.info("[NOTIFICATION] Email denda berhasil dikirim ke: {}", anggota.getEmail());
            }
        } catch (Exception e) {
            log.error("[NOTIFICATION_ERROR] Gagal mengirim email: {}", e.getMessage());
        }
    }
}