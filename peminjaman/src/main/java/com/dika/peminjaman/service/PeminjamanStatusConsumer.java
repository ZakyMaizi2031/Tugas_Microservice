package com.dika.peminjaman.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dika.peminjaman.config.RabbitMQConfig;
import com.dika.peminjaman.dto.PengembalianEvent;
import com.dika.peminjaman.cqrs.command.model.Peminjaman;
import com.dika.peminjaman.repository.PeminjamanJpaRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PeminjamanStatusConsumer {

    @Autowired
    private PeminjamanJpaRepository peminjamanRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.STATUS_QUEUE)
    public void receiveReturnEvent(PengembalianEvent event) {
        log.info("RABBITMQ: Menerima sinyal pengembalian untuk ID: {}", event.getIdPeminjaman());

        peminjamanRepository.findById(event.getIdPeminjaman()).ifPresentOrElse(peminjaman -> {
            // 1. Update ke MySQL
            peminjaman.setStatus("SELESAI");
            Peminjaman updated = peminjamanRepository.save(peminjaman);
            log.info("SUKSES: Peminjaman ID {} telah ditutup di MySQL", updated.getId());

            // 2. Trigger Sinkronisasi ke MongoDB (Internal)
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "peminjaman.sync", updated);
        }, () -> log.error("ERROR: Data Peminjaman tidak ditemukan untuk ID: {}", event.getIdPeminjaman()));
    }
}