package com.zaky.peminjaman.cqrs.query.handler;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.zaky.peminjaman.model.Peminjaman;
import com.zaky.peminjaman.repository.PeminjamanRepository;

@Service
public class PeminjamanQueryHandler {

    private final PeminjamanRepository repository;

    public PeminjamanQueryHandler(PeminjamanRepository repository) {
        this.repository = repository;
    }

    public List<Peminjaman> getAll() {
        return repository.findAll();
    }

    public Optional<Peminjaman> getById(Long id) {
        return repository.findById(id);
    }
}
