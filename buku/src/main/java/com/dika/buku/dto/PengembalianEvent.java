package com.dika.buku.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PengembalianEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long idPengembalian;
    private String tanggalDikembalikan;
    private double denda;
    private Long idPeminjaman;
    private Long idBuku;
    private Long idAnggota;
    private String status;

    
}