package com.dika.peminjaman.cqrs.query.handler;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dika.peminjaman.cqrs.query.model.PeminjamanDocument;
import com.dika.peminjaman.repository.PeminjamanMongoRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j // Aktifkan logging
public class PeminjamanQueryHandler {

    @Autowired private PeminjamanMongoRepository mongoRepo;

    public List<PeminjamanDocument> handleGetAll() {
        log.info("[QUERY] Menarik semua data history peminjaman dari MongoDB...");
        
        List<PeminjamanDocument> list = mongoRepo.findAll();
        
        log.info("[QUERY] Berhasil mengambil {} data history peminjaman", list.size());
        return list;
    }

    public Optional<PeminjamanDocument> handleGetById(String id) {
        log.info("[QUERY] Mencari detail transaksi peminjaman ID: {}", id);
        
        Optional<PeminjamanDocument> result = mongoRepo.findById(id);
        
        if (result.isPresent()) {
            log.info("[QUERY] Data transaksi ditemukan. Status: {}", result.get().getStatus());
        } else {
            log.warn("[QUERY_WARN] Transaksi ID {} tidak ditemukan di MongoDB", id);
        }
        
        return result;
    }
}