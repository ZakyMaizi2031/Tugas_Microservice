package com.zaky.pengembalian_service.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.zaky.pengembalian_service.model.Pengembalian;
import com.zaky.pengembalian_service.repository.PengembalianRepository;
import com.zaky.pengembalian_service.vo.Anggota;
import com.zaky.pengembalian_service.vo.Buku;
import com.zaky.pengembalian_service.vo.Peminjaman;
import com.zaky.pengembalian_service.vo.ResponseTemplate;

@Service
public class PengembalianService {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private PengembalianRepository pengembalianRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EmailDendaService emailDendaService;

    private static final double DENDA_PER_HARI = 2000;

    // Nama Service harus lowercase (sesuai spring.application.name)
    private static final String PEMINJAMAN_SERVICE_ID = "peminjaman";
    private static final String ANGGOTA_SERVICE_ID = "anggota";
    private static final String BUKU_SERVICE_ID = "buku";
    
    // Helper untuk mendapatkan Base URL dari service
    private String getBaseUrl(String serviceId) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        if (instances.isEmpty()) {
            // Menggunakan serviceId yang sebenarnya dalam pesan error
            throw new IllegalStateException("Service " + serviceId.toUpperCase() + " tidak tersedia di Discovery Client"); 
        }
        return instances.get(0).getUri().toString();
    }

    /**
     * Buat pengembalian baru dengan tanggal dikembalikan manual
     */
    public Pengembalian createPengembalian(Pengembalian pengembalian) {
        if (pengembalian.getTanggal_dikembalikan() == null || pengembalian.getTanggal_dikembalikan().isEmpty()) {
            throw new IllegalArgumentException("Tanggal pengembalian harus diinput");
        }

        // --- Ambil service instance PEMINJAMAN ---
        String baseUrlPeminjaman;
        try {
            baseUrlPeminjaman = getBaseUrl(PEMINJAMAN_SERVICE_ID);
        } catch (IllegalStateException e) {
            // Meneruskan error jika service tidak ditemukan
            throw new IllegalStateException(e.getMessage()); 
        }

        String peminjamanUrl = baseUrlPeminjaman + "/api/peminjaman/" + pengembalian.getPeminjamanId();
        Peminjaman peminjaman;
        
        System.out.println("DEBUG: Panggil Peminjaman URL: " + peminjamanUrl); // LOG Tambahan
        
        try {
            peminjaman = restTemplate.getForObject(peminjamanUrl, Peminjaman.class);
        } catch (RestClientException e) {
            throw new IllegalStateException("Gagal memanggil service PEMINJAMAN: " + e.getMessage() + ". URL: " + peminjamanUrl);
        }

        // BARIS KRITIS (sebelumnya di baris 58)
        if (peminjaman == null || peminjaman.getTanggalPinjam() == null) {
             System.err.println("DEBUG: Hasil Peminjaman null? " + (peminjaman == null) + 
                                ". Tanggal Pinjam null? " + (peminjaman != null && peminjaman.getTanggalPinjam() == null)); // LOG Tambahan
            throw new IllegalArgumentException("Tanggal pinjam tidak ditemukan pada data peminjaman ID: " + pengembalian.getPeminjamanId());
        }

        // --- Parsing tanggal pinjam dan dikembalikan ---
        LocalDate tanggalPinjam = parseTanggal(peminjaman.getTanggalPinjam());
        LocalDate tanggalDikembalikan = parseTanggal(pengembalian.getTanggal_dikembalikan());

        // --- Hitung selisih hari ---
        long terlambatHari = ChronoUnit.DAYS.between(tanggalPinjam, tanggalDikembalikan);
        if (terlambatHari < 0) terlambatHari = 0;

        pengembalian.setTerlambat(String.valueOf(terlambatHari));
        pengembalian.setDenda(terlambatHari * DENDA_PER_HARI);

        Pengembalian savedPengembalian = pengembalianRepository.save(pengembalian);

        // --- Kirim email denda jika ada keterlambatan ---
        if (terlambatHari > 0) {
            kirimEmailDendaJikaPerlu(peminjaman, terlambatHari);
        }

        return savedPengembalian;
    }

    private LocalDate parseTanggal(String tanggalStr) {
        // Mendukung dua format umum
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        try {
            return LocalDate.parse(tanggalStr, formatter1);
        } catch (Exception e1) {
            try {
                return LocalDate.parse(tanggalStr, formatter2);
            } catch (Exception e2) {
                throw new IllegalArgumentException("Format tanggal tidak valid: " + tanggalStr +
                        ". Gunakan format yyyy-MM-dd atau dd-MM-yyyy");
            }
        }
    }

    private void kirimEmailDendaJikaPerlu(Peminjaman peminjaman, long terlambatHari) {
        try {
            Anggota anggota = null;
            Buku buku = null;

            // --- Ambil data anggota ---
            String baseUrlAnggota = getBaseUrl(ANGGOTA_SERVICE_ID);
            if (peminjaman.getAnggotaId() != null) {
                String anggotaUrl = baseUrlAnggota + "/api/anggota/" + peminjaman.getAnggotaId();
                anggota = restTemplate.getForObject(anggotaUrl, Anggota.class);
            }

            // --- Ambil data buku ---
            String baseUrlBuku = getBaseUrl(BUKU_SERVICE_ID);
            if (peminjaman.getBukuId() != null) {
                String bukuUrl = baseUrlBuku + "/api/buku/" + peminjaman.getBukuId();
                buku = restTemplate.getForObject(bukuUrl, Buku.class);
            }

            if (anggota != null && buku != null) {
                emailDendaService.kirimEmailDenda(
                        anggota.getEmail(),
                        anggota.getNama(),
                        buku.getJudul(),
                        terlambatHari,
                        terlambatHari * DENDA_PER_HARI
                );
            }
        } catch (Exception e) {
            System.err.println("Gagal mengirim email denda: " + e.getMessage());
        }
    }

    // --- Ambil semua pengembalian tanpa detail ---
    public List<Pengembalian> getAllPengembalian() {
        return pengembalianRepository.findAll();
    }

    // --- Ambil 1 pengembalian by ID ---
    public Pengembalian getPengembalianById(Long id) {
        return pengembalianRepository.findById(id).orElse(null);
    }

    // --- Ambil pengembalian lengkap dengan detail ---
    public ResponseTemplate getPengembalianWithDetailsById(Long id) {
        Pengembalian pengembalian = getPengembalianById(id);
        if (pengembalian == null) return null;

        String baseUrlPeminjaman;
        try {
            baseUrlPeminjaman = getBaseUrl(PEMINJAMAN_SERVICE_ID);
        } catch (IllegalStateException e) {
            throw new IllegalStateException(e.getMessage()); 
        }

        Peminjaman peminjaman = restTemplate.getForObject(
                baseUrlPeminjaman + "/api/peminjaman/" + pengembalian.getPeminjamanId(),
                Peminjaman.class);

        Anggota anggota = null;
        Buku buku = null;

        if (peminjaman != null) {
            // --- Ambil data anggota ---
            String baseUrlAnggota = getBaseUrl(ANGGOTA_SERVICE_ID);
            if (peminjaman.getAnggotaId() != null) {
                anggota = restTemplate.getForObject(
                        baseUrlAnggota + "/api/anggota/" + peminjaman.getAnggotaId(),
                        Anggota.class);
            }

            // --- Ambil data buku ---
            String baseUrlBuku = getBaseUrl(BUKU_SERVICE_ID);
            if (peminjaman.getBukuId() != null) {
                buku = restTemplate.getForObject(
                        baseUrlBuku + "/api/buku/" + peminjaman.getBukuId(),
                        Buku.class);
            }
        }

        return new ResponseTemplate(peminjaman, anggota, buku, pengembalian);
    }

    // --- Ambil semua pengembalian lengkap ---
    public List<ResponseTemplate> getAllPengembalianWithDetails() {
        List<ResponseTemplate> responseList = new ArrayList<>();
        for (Pengembalian pengembalian : pengembalianRepository.findAll()) {
            ResponseTemplate response = getPengembalianWithDetailsById(pengembalian.getId());
            if (response != null) responseList.add(response);
        }
        return responseList;
    }

    // --- Hapus pengembalian ---
    public void deletePengembalian(Long id) {
        pengembalianRepository.deleteById(id);
    }
}