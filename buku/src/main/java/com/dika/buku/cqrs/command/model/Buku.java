package com.dika.buku.cqrs.command.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "buku_write")
@NoArgsConstructor  // WAJIB untuk Jackson
@AllArgsConstructor // Bagus untuk pembuatan objek
public class Buku {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String judul;
    private String pengarang;
    private String penerbit;
    private String tahun_terbit;
    private String status;
}