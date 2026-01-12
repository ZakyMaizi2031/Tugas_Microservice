package com.dika.pengembalian_service.cqrs.command.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "pengembalian_write")
@NoArgsConstructor
@AllArgsConstructor
public class Pengembalian {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long peminjamanId;
    private String tanggal_dikembalikan;
    private String terlambat;
    private double denda;
}