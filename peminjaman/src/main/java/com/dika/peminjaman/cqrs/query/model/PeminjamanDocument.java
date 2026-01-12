package com.dika.peminjaman.cqrs.query.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "peminjaman_read")
public class PeminjamanDocument {
    @Id
    private String id;
    private Long anggotaId;
    private Long bukuId;
    private String tanggalPinjam;
    private String status;
}