package com.zaky.pengembalian_service.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Peminjaman {
    private Long id;
    private Long anggotaId;
    private Long bukuId;

    // Gunakan @JsonProperty agar cocok dengan field JSON dari peminjaman-service
    @JsonProperty("tanggalPinjam")
    private String tanggalPinjam;

    // ✅ Wajib: constructor kosong agar Jackson bisa melakukan deserialisasi JSON
    public Peminjaman() {}

    public Peminjaman(Long id, Long anggotaId, Long bukuId, String tanggalPinjam) {
        this.id = id;
        this.anggotaId = anggotaId;
        this.bukuId = bukuId;
        this.tanggalPinjam = tanggalPinjam;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "Peminjaman{" +
                "id=" + id +
                ", anggotaId=" + anggotaId +
                ", bukuId=" + bukuId +
                ", tanggalPinjam='" + tanggalPinjam + '\'' +
                '}';
    }
}
