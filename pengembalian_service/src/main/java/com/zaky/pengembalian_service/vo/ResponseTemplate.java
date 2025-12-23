package com.zaky.pengembalian_service.vo;

import com.zaky.pengembalian_service.model.Pengembalian;

public class ResponseTemplate {

    private Peminjaman peminjaman;
    private Anggota anggota;
    private Buku buku;
    private Pengembalian pengembalian;

    public ResponseTemplate() {
    }

    public ResponseTemplate(Peminjaman peminjaman, Anggota anggota, Buku buku, Pengembalian pengembalian) {
        this.peminjaman = peminjaman;
        this.anggota = anggota;
        this.buku = buku;
        this.pengembalian = pengembalian;
    }

    // --- Getters ---

    public Peminjaman getPeminjaman() {
        return peminjaman;
    }

    public Anggota getAnggota() {
        return anggota;
    }

    public Buku getBuku() {
        return buku;
    }

    public Pengembalian getPengembalian() {
        return pengembalian;
    }

    // --- Setters ---

    public void setPeminjaman(Peminjaman peminjaman) {
        this.peminjaman = peminjaman;
    }

    public void setAnggota(Anggota anggota) {
        this.anggota = anggota;
    }

    public void setBuku(Buku buku) {
        this.buku = buku;
    }

    public void setPengembalian(Pengembalian pengembalian) {
        this.pengembalian = pengembalian;
    }
}