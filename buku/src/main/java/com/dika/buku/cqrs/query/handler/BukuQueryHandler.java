package com.dika.buku.cqrs.query.handler;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired; // Import Slf4j
import org.springframework.stereotype.Service;

import com.dika.buku.cqrs.query.model.BukuDocument;
import com.dika.buku.repository.BukuMongoRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j // Aktifkan logging
public class BukuQueryHandler {

    @Autowired private BukuMongoRepository mongoRepo;

    public List<BukuDocument> handleGetAll() {
        log.info("[QUERY] Mengambil semua daftar buku dari MongoDB...");
        
        List<BukuDocument> list = mongoRepo.findAll();
        
        log.info("[QUERY] Berhasil menarik {} data buku dari MongoDB", list.size());
        return list;
    }

    public Optional<BukuDocument> handleGetById(String id) {
        log.info("[QUERY] Mencari detail Buku berdasarkan ID: {}", id);
        
        Optional<BukuDocument> result = mongoRepo.findById(id);
        
        if (result.isPresent()) {
            log.info("[QUERY] Data ditemukan: '{}' oleh {}", result.get().getJudul(), result.get().getPengarang());
        } else {
            log.warn("[QUERY_WARN] Buku dengan ID {} tidak ditemukan di MongoDB", id);
        }
        
        return result;
    }
}