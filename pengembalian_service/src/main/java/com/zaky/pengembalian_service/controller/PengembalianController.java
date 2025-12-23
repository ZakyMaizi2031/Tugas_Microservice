package com.zaky.pengembalian_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zaky.pengembalian_service.model.Pengembalian;
import com.zaky.pengembalian_service.service.EmailDendaService;
import com.zaky.pengembalian_service.service.PengembalianService;
import com.zaky.pengembalian_service.vo.ResponseTemplate;

@RestController
@RequestMapping("/api/pengembalian")
public class PengembalianController {

    @Autowired
    private PengembalianService pengembalianService;
    @Autowired
    private EmailDendaService emailDendaService;

    // ✅ Ambil semua pengembalian (tanpa detail anggota, buku & peminjaman)
    @GetMapping
    public List<Pengembalian> getAllPengembalian() {
        return pengembalianService.getAllPengembalian();
    }

    // ✅ Ambil 1 pengembalian lengkap dengan detail anggota, buku & peminjaman
    // URL: http://localhost:8083/api/pengembalian/1
    @GetMapping("/{id}")
    public ResponseEntity<ResponseTemplate> getPengembalianWithDetailsById(@PathVariable Long id) {
        ResponseTemplate response = pengembalianService.getPengembalianWithDetailsById(id);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ Buat pengembalian baru
    @PostMapping
    public Pengembalian createPengembalian(@RequestBody Pengembalian pengembalian) {
        // Simpan pengembalian dan hitung denda otomatis
        Pengembalian saved = pengembalianService.createPengembalian(pengembalian);

        // Ambil detail lengkap (anggota & buku) untuk email
        ResponseTemplate details = pengembalianService.getPengembalianWithDetailsById(saved.getId());

        if (details != null) {
            long hariTerlambat = Long.parseLong(saved.getTerlambat());
            double totalDenda = saved.getDenda();

            // Kirim email hanya jika ada keterlambatan
            if (hariTerlambat > 0) {
                emailDendaService.kirimEmailDenda(
                        details.getAnggota().getEmail(),
                        details.getAnggota().getNama(),
                        details.getBuku().getJudul(),
                        hariTerlambat,
                        totalDenda);
            }
        }

        return saved;
    }

    // ✅ Hapus pengembalian
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePengembalian(@PathVariable Long id) {
        pengembalianService.deletePengembalian(id);
        return ResponseEntity.ok().build();
    }
}