package com.dika.anggota.controller;

import com.dika.anggota.cqrs.command.handler.AnggotaCommandHandler;
import com.dika.anggota.cqrs.command.model.Anggota;
import com.dika.anggota.cqrs.query.handler.AnggotaQueryHandler;
import com.dika.anggota.cqrs.query.model.AnggotaDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/anggota")
public class AnggotaController {
    @Autowired private AnggotaCommandHandler commandHandler;
    @Autowired private AnggotaQueryHandler queryHandler;

    @PostMapping
    public ResponseEntity<Anggota> create(@RequestBody Anggota anggota) {
        return ResponseEntity.ok(commandHandler.handleCreate(anggota));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Anggota> update(@PathVariable Long id, @RequestBody Anggota anggota) {
        return ResponseEntity.ok(commandHandler.handleUpdate(id, anggota));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        commandHandler.handleDelete(id);
        return ResponseEntity.ok("Anggota Berhasil Dihapus");
    }

    @GetMapping
    public ResponseEntity<List<AnggotaDocument>> getAll() {
        return ResponseEntity.ok(queryHandler.handleGetAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnggotaDocument> getById(@PathVariable String id) {
        return queryHandler.handleGetById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}