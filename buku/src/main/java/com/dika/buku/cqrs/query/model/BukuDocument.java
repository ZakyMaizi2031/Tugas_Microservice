package com.dika.buku.cqrs.query.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "buku_read")
public class BukuDocument {
    @Id
    private String id; // Akan diisi String dari ID MySQL
    private String judul;
    private String pengarang;
    private String penerbit;     // <--- Ditambahkan
    private String tahun_terbit; // <--- Ditambahkan
    private String status;
}