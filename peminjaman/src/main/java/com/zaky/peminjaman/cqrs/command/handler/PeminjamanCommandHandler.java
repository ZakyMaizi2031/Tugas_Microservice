package com.zaky.peminjaman.cqrs.command.handler;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.zaky.peminjaman.config.RabbitMQConfig;
import com.zaky.peminjaman.cqrs.command.CreatePeminjamanCommand;
import com.zaky.peminjaman.model.Peminjaman;
import com.zaky.peminjaman.repository.PeminjamanRepository;

@Service
public class PeminjamanCommandHandler {

    private final PeminjamanRepository repository;
    private final RabbitTemplate rabbitTemplate;

    public PeminjamanCommandHandler(PeminjamanRepository repository, RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Peminjaman handle(CreatePeminjamanCommand command) {
        // langsung pakai String tanggalPinjam
        Peminjaman peminjaman = new Peminjaman(
                command.getAnggotaId(),
                command.getBukuId(),
                command.getTanggalPinjam()
        );

        Peminjaman saved = repository.save(peminjaman);

        // Kirim event ke RabbitMQ
        String payload = saved.getAnggotaId() + "|" + saved.getBukuId();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE, payload);

        return saved;
    }
}
