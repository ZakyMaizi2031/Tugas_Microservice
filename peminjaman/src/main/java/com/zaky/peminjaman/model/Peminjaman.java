package com.zaky.peminjaman.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "peminjaman")
public class Peminjaman {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long anggotaId;
    private Long bukuId;

    @Column(name = "tanggal_pinjam")
    private String tanggalPinjam; // pakai String

    // ✅ Wajib ada constructor kosong (untuk JPA)
    public Peminjaman() {}

    // ✅ Constructor dengan parameter
    public Peminjaman(Long anggotaId, Long bukuId, String tanggalPinjam) {
        this.anggotaId = anggotaId;
        this.bukuId = bukuId;
        this.tanggalPinjam = tanggalPinjam;
    }

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public Long getAnggotaId() {
        return anggotaId;
    }

    public void setAnggotaId(Long anggotaId) {
        this.anggotaId = anggotaId;
    }

    public Long getBukuId() {
        return bukuId;
    }

    public void setBukuId(Long bukuId) {
        this.bukuId = bukuId;
    }

    public String getTanggalPinjam() {
        return tanggalPinjam;
    }

    public void setTanggalPinjam(String tanggalPinjam) {
        this.tanggalPinjam = tanggalPinjam;
    }
}
