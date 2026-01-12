package com.dika.peminjaman.controller;

import com.dika.peminjaman.cqrs.command.handler.PeminjamanCommandHandler;
import com.dika.peminjaman.cqrs.command.model.Peminjaman;
import com.dika.peminjaman.cqrs.query.handler.PeminjamanQueryHandler;
import com.dika.peminjaman.cqrs.query.model.PeminjamanDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/peminjaman")
public class PeminjamanController {
    @Autowired private PeminjamanCommandHandler commandHandler;
    @Autowired private PeminjamanQueryHandler queryHandler;

    @PostMapping
    public ResponseEntity<Peminjaman> create(@RequestBody Peminjaman peminjaman) {
        return ResponseEntity.ok(commandHandler.handleCreate(peminjaman));
    }

    @GetMapping
    public ResponseEntity<List<PeminjamanDocument>> getAll() {
        return ResponseEntity.ok(queryHandler.handleGetAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PeminjamanDocument> getById(@PathVariable String id) {
        return queryHandler.handleGetById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        commandHandler.handleDelete(id);
        return ResponseEntity.ok("Peminjaman Berhasil Dihapus");
    }
}