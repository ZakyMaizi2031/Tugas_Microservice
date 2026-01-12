package com.dika.peminjaman.consumer;

import com.dika.peminjaman.dto.PengembalianEvent;
import com.dika.peminjaman.cqrs.command.model.Peminjaman;
import com.dika.peminjaman.repository.PeminjamanJpaRepository;
import com.dika.peminjaman.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PeminjamanConsumer {

    @Autowired
    private PeminjamanJpaRepository jpaRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Listener ini menangkap pesan dari Service Pengembalian
     */
    @RabbitListener(queues = "q.peminjaman.update-status")
    public void handleUpdateStatus(PengembalianEvent event) {
        log.info("RABBITMQ: Menerima notifikasi pengembalian untuk ID Peminjaman: {}", event.getIdPeminjaman());

        // 1. UPDATE DATA DI MYSQL (Command Side)
        jpaRepository.findById(event.getIdPeminjaman()).ifPresentOrElse(peminjaman -> {
            peminjaman.setStatus("SELESAI");
            Peminjaman updatedPeminjaman = jpaRepository.save(peminjaman);
            log.info("SUKSES: MySQL - Transaksi Peminjaman ID {} telah ditutup (SELESAI).", peminjaman.getId());

            // 2. TRIGGER SINKRONISASI KE MONGODB (Query Side)
            // Kita kirim pesan ke antrean sync internal kita sendiri
            rabbitTemplate.convertAndSend("x.perpustakaan", "peminjaman.sync", updatedPeminjaman);
            log.info("SYNC: Mengirim sinyal update status ke MongoDB untuk ID: {}", updatedPeminjaman.getId());

        }, () -> {
            log.error("GAGAL: Data Peminjaman ID {} tidak ditemukan di MySQL.", event.getIdPeminjaman());
        });
    }
}