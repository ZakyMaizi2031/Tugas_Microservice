package com.dika.pengembalian_service.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PengembalianEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    // Data dari Model Pengembalian
    private Long idPengembalian;
    private String tanggalDikembalikan;
    private double denda;

    // Data Relasi yang didapat dari VO Peminjaman
    private Long idPeminjaman;
    private Long idBuku;
    private Long idAnggota;
    
    // Status untuk trigger di service lain (misal: "SUCCESS")
    private String status;
}