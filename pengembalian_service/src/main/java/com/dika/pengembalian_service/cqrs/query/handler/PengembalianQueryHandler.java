package com.dika.pengembalian_service.cqrs.query.handler;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dika.pengembalian_service.cqrs.query.model.PengembalianDocument;
import com.dika.pengembalian_service.repository.PengembalianMongoRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j // Aktifkan logging
public class PengembalianQueryHandler {

    @Autowired private PengembalianMongoRepository mongoRepo;

    public List<PengembalianDocument> handleGetAll() {
        log.info("[QUERY] Menarik semua history pengembalian dari MongoDB...");
        List<PengembalianDocument> list = mongoRepo.findAll();
        log.info("[QUERY] Berhasil mengambil {} data history", list.size());
        return list;
    }

    public Optional<PengembalianDocument> handleGetById(String id) {
        log.info("[QUERY] Mencari detail pengembalian ID: {}", id);
        Optional<PengembalianDocument> result = mongoRepo.findById(id);
        
        if (result.isPresent()) {
            log.info("[QUERY] Data ditemukan untuk Peminjaman ID: {}", result.get().getPeminjamanId());
        } else {
            log.warn("[QUERY_WARN] Data Pengembalian ID {} tidak ditemukan di MongoDB", id);
        }
        return result;
    }
}