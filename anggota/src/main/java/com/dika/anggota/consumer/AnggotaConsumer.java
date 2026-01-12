package com.dika.anggota.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dika.anggota.dto.PengembalianEvent;
import com.dika.anggota.repository.AnggotaJpaRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AnggotaConsumer {

    @Autowired
    private AnggotaJpaRepository anggotaRepository;

    @RabbitListener(queues = "q.anggota.update-status")
    public void handleAnggotaUpdate(PengembalianEvent event) {
        // 1. VALIDASI NULL (Pencegah Looping)
        if (event == null || event.getIdAnggota() == null) {
            log.error("RABBITMQ_ERROR: Menerima pesan NULL atau ID Anggota Kosong. Pesan diabaikan agar tidak looping.");
            return; // Kita return biasa (ACK), supaya pesan dihapus dari antrean oleh RabbitMQ
        }

        log.info("RABBITMQ_RECEIVER: Memproses Anggota ID: {}", event.getIdAnggota());

        try {
            // 2. Gunakan findById dengan aman
            anggotaRepository.findById(event.getIdAnggota()).ifPresentOrElse(anggota -> {
                
                if (event.getDenda() <= 0) {
                    log.info("AUDIT_LOG: Anggota {} dalam status aman (tanpa denda).", anggota.getNama());
                } else {
                    log.info("AUDIT_LOG: Anggota {} mengembalikan dengan denda Rp {}", anggota.getNama(), event.getDenda());
                }
                
                anggotaRepository.save(anggota);
                
            }, () -> log.warn("AUDIT_LOG: Data Anggota ID {} tidak ditemukan di database.", event.getIdAnggota()));
            
        } catch (Exception e) {
            log.error("SYSTEM_ERROR: Terjadi kesalahan saat akses database: {}", e.getMessage());
            // Jangan lempar exception (throw) ke atas agar tidak re-queue terus menerus
        }
    }
}