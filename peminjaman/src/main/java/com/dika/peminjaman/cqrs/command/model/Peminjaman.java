package com.dika.peminjaman.cqrs.command.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "peminjaman_write")
public class Peminjaman {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long anggotaId;
    private Long bukuId;
    private String tanggalPinjam;
    private String status; // "DIPINJAM", "SELESAI"
}