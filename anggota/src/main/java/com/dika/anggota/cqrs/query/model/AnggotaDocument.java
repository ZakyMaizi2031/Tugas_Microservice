package com.dika.anggota.cqrs.query.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "anggota_read")
public class AnggotaDocument {
    @Id
    private String id; // Berisi ID dari MySQL
    private String nim;
    private String nama;
    private String email;
    private String alamat;
    private String jenis_kelamin;
}