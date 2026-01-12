package com.dika.buku.controller;

import com.dika.buku.cqrs.command.handler.BukuCommandHandler;
import com.dika.buku.cqrs.command.model.Buku;
import com.dika.buku.cqrs.query.handler.BukuQueryHandler;
import com.dika.buku.cqrs.query.model.BukuDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/buku")
public class BukuController {
    @Autowired private BukuCommandHandler commandHandler;
    @Autowired private BukuQueryHandler queryHandler;

    @PostMapping
    public ResponseEntity<Buku> create(@RequestBody Buku buku) {
        return ResponseEntity.ok(commandHandler.handleCreate(buku));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Buku> update(@PathVariable Long id, @RequestBody Buku buku) {
        return ResponseEntity.ok(commandHandler.handleUpdate(id, buku));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        commandHandler.handleDelete(id);
        return ResponseEntity.ok("Buku Berhasil Dihapus");
    }

    @GetMapping
    public ResponseEntity<List<BukuDocument>> getAll() {
        return ResponseEntity.ok(queryHandler.handleGetAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BukuDocument> getById(@PathVariable String id) {
        return queryHandler.handleGetById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}