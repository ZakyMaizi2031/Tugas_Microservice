package com.zaky.peminjaman.cqrs.command;

public class CreatePeminjamanCommand {
    private Long anggotaId;
    private Long bukuId;
    private String tanggalPinjam; // dikirim dalam format "yyyy-MM-dd"

    public CreatePeminjamanCommand() {}

    public CreatePeminjamanCommand(Long anggotaId, Long bukuId, String tanggalPinjam) {
        this.anggotaId = anggotaId;
        this.bukuId = bukuId;
        this.tanggalPinjam = tanggalPinjam;
    }

    public Long getAnggotaId() { return anggotaId; }
    public void setAnggotaId(Long anggotaId) { this.anggotaId = anggotaId; }

    public Long getBukuId() { return bukuId; }
    public void setBukuId(Long bukuId) { this.bukuId = bukuId; }

    public String getTanggalPinjam() { return tanggalPinjam; }
    public void setTanggalPinjam(String tanggalPinjam) { this.tanggalPinjam = tanggalPinjam; }
}
