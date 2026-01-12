package com.dika.anggota.cqrs.query.handler;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired; // Import Lombok Slf4j
import org.springframework.stereotype.Service;

import com.dika.anggota.cqrs.query.model.AnggotaDocument;
import com.dika.anggota.repository.AnggotaMongoRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j // Aktifkan logging
public class AnggotaQueryHandler {

    @Autowired private AnggotaMongoRepository mongoRepo;

    public List<AnggotaDocument> handleGetAll() {
        log.info("[QUERY] Mengambil semua data anggota dari MongoDB...");
        
        List<AnggotaDocument> list = mongoRepo.findAll();
        
        log.info("[QUERY] Berhasil menarik {} data anggota dari MongoDB", list.size());
        return list;
    }

    public Optional<AnggotaDocument> handleGetById(String id) {
        log.info("[QUERY] Mencari detail Anggota berdasarkan ID: {}", id);
        
        Optional<AnggotaDocument> result = mongoRepo.findById(id);
        
        if (result.isPresent()) {
            log.info("[QUERY] Data ditemukan: {}", result.get().getNama());
        } else {
            log.warn("[QUERY_WARN] Data Anggota dengan ID {} tidak ditemukan di MongoDB", id);
        }
        
        return result;
    }
}