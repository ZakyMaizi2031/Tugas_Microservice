package com.dika.anggota.cqrs.command.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "anggota_write")
public class Anggota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nim;
    private String nama;
    private String email;
    private String alamat;
    private String jenis_kelamin;
}