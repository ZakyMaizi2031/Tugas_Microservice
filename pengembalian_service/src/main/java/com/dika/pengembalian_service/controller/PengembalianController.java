package com.dika.pengembalian_service.controller;

import com.dika.pengembalian_service.cqrs.command.handler.PengembalianCommandHandler;
import com.dika.pengembalian_service.cqrs.command.model.Pengembalian;
import com.dika.pengembalian_service.cqrs.query.handler.PengembalianQueryHandler;
import com.dika.pengembalian_service.cqrs.query.model.PengembalianDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pengembalian")
public class PengembalianController {

    @Autowired private PengembalianCommandHandler commandHandler;
    @Autowired private PengembalianQueryHandler queryHandler;

    @PostMapping
    public ResponseEntity<Pengembalian> create(@RequestBody Pengembalian p) {
        return ResponseEntity.ok(commandHandler.handleCreate(p));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pengembalian> update(@PathVariable Long id, @RequestBody Pengembalian p) {
        return ResponseEntity.ok(commandHandler.handleUpdate(id, p));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        commandHandler.handleDelete(id);
        return ResponseEntity.ok("Data Pengembalian Berhasil Dihapus");
    }

    @GetMapping
    public ResponseEntity<List<PengembalianDocument>> getAll() {
        return ResponseEntity.ok(queryHandler.handleGetAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PengembalianDocument> getById(@PathVariable String id) {
        return queryHandler.handleGetById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}