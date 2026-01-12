package com.dika.buku.cqrs.command.handler;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dika.buku.cqrs.command.model.Buku;
import com.dika.buku.repository.BukuJpaRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j // Aktifkan logging profesional
public class BukuCommandHandler {

    @Autowired private BukuJpaRepository jpaRepo;
    @Autowired private RabbitTemplate rabbitTemplate;

    @Transactional
    public Buku handleCreate(Buku buku) {
        log.info("[COMMAND] Memproses pembuatan buku baru: Judul='{}', Pengarang='{}'", buku.getJudul(), buku.getPengarang());
        
        buku.setStatus("Tersedia");
        Buku saved = jpaRepo.save(buku);
        
        log.info("[COMMAND] Berhasil simpan ke MySQL. ID: {}", saved.getId());

        // Kirim objek bersih ke RabbitMQ
        sendSyncMessage(saved);
        return saved;
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        if (id == null) {
            log.error("[COMMAND_ERROR] Gagal update status: ID Buku bernilai NULL");
            return;
        }

        log.info("[COMMAND] Menerima instruksi update status Buku ID: {} menjadi '{}'", id, status);

        jpaRepo.findById(id).ifPresentOrElse(buku -> {
            buku.setStatus(status);
            Buku updated = jpaRepo.save(buku);
            
            log.info("[COMMAND] MySQL Update Sukses: Buku ID {} kini berstatus '{}'", id, status);
            
            // Kirim objek bersih ke RabbitMQ untuk sinkronisasi MongoDB
            sendSyncMessage(updated);
        }, () -> {
            log.error("[COMMAND_ERROR] Update gagal: Buku ID {} tidak ditemukan di database MySQL", id);
        });
    }

    @Transactional
    public Buku handleUpdate(Long id, Buku bukuReq) {
        log.info("[COMMAND] Memproses update data lengkap untuk Buku ID: {}", id);

        return jpaRepo.findById(id).map(buku -> {
            buku.setJudul(bukuReq.getJudul());
            buku.setPengarang(bukuReq.getPengarang());
            buku.setPenerbit(bukuReq.getPenerbit());
            buku.setTahun_terbit(bukuReq.getTahun_terbit());
            buku.setStatus(bukuReq.getStatus());
            
            Buku updated = jpaRepo.save(buku);
            log.info("[COMMAND] Berhasil update MySQL untuk Buku ID: {}", id);
            
            sendSyncMessage(updated);
            return updated;
        }).orElseThrow(() -> {
            log.error("[COMMAND_ERROR] Gagal update: Buku ID {} tidak ditemukan", id);
            return new RuntimeException("Buku tidak ditemukan");
        });
    }

    @Transactional
    public void handleDelete(Long id) {
        log.info("[COMMAND] Memproses penghapusan Buku ID: {}", id);

        if(id != null && jpaRepo.existsById(id)) {
            jpaRepo.deleteById(id);
            log.info("[COMMAND] Berhasil menghapus Buku ID {} dari MySQL", id);

            log.info("[SYNC] Mengirim sinyal hapus ke RabbitMQ untuk Buku ID: {}", id);
            rabbitTemplate.convertAndSend("x.perpustakaan", "buku.delete", id);
        } else {
            log.warn("[COMMAND_WARN] Percobaan hapus gagal: Buku ID {} tidak ditemukan", id);
        }
    }

    // HELPER: Mengonversi Entity ke Objek baru agar tidak terkena masalah Hibernate Proxy
    private void sendSyncMessage(Buku buku) {
        log.info("[SYNC] Menyiapkan data sinkronisasi untuk Buku ID: {}", buku.getId());
        
        Buku cleanBuku = new Buku();
        cleanBuku.setId(buku.getId());
        cleanBuku.setJudul(buku.getJudul());
        cleanBuku.setPengarang(buku.getPengarang());
        cleanBuku.setPenerbit(buku.getPenerbit());
        cleanBuku.setTahun_terbit(buku.getTahun_terbit());
        cleanBuku.setStatus(buku.getStatus());

        rabbitTemplate.convertAndSend("x.perpustakaan", "buku.sync", cleanBuku);
        log.info("[SYNC] Pesan sinkronisasi dikirim ke RabbitMQ untuk Buku: '{}'", cleanBuku.getJudul());
    }
}