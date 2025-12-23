package com.zaky.peminjaman.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zaky.peminjaman.cqrs.command.CreatePeminjamanCommand;
import com.zaky.peminjaman.cqrs.command.handler.PeminjamanCommandHandler;
import com.zaky.peminjaman.cqrs.query.handler.PeminjamanQueryHandler;
import com.zaky.peminjaman.model.Peminjaman;

@RestController
@RequestMapping("/api/peminjaman")
public class PeminjamanController {

    private final PeminjamanCommandHandler commandHandler;
    private final PeminjamanQueryHandler queryHandler;

    public PeminjamanController(PeminjamanCommandHandler commandHandler,
                                PeminjamanQueryHandler queryHandler) {
        this.commandHandler = commandHandler;
        this.queryHandler = queryHandler;
    }

    @PostMapping
    public Peminjaman create(@RequestBody CreatePeminjamanCommand command) {
        return commandHandler.handle(command);
    }

    @GetMapping
    public List<Peminjaman> getAll() {
        return queryHandler.getAll();
    }

    @GetMapping("/{id}")
    public Peminjaman getById(@PathVariable Long id) {
        return queryHandler.getById(id).orElse(null);
    }
}
