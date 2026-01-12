package com.dika.pengembalian_service.cqrs.query.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "pengembalian_read")
public class PengembalianDocument {
    @Id
    private String id; // ID dari MySQL
    private Long peminjamanId;
    private String tanggal_dikembalikan;
    private String terlambat;
    private double denda;
}