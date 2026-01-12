package com.dika.buku.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dika.buku.dto.PengembalianEvent;
import com.dika.buku.repository.BukuJpaRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BukuConsumer {

    @Autowired
    private BukuJpaRepository bukuRepository;

    // Mendengarkan antrean q.buku.update-status
    @RabbitListener(queues = "q.buku.update-status")
    public void handleUpdateBukuStatus(PengembalianEvent event) {
        log.info("RABBITMQ_RECEIVER: Menerima event update status untuk Buku ID: {} | Status Event: {}", 
                 event.getIdBuku(), event.getStatus());

        // Cari buku di database berdasarkan ID
        bukuRepository.findById(event.getIdBuku()).ifPresentOrElse(buku -> {
            
            // Ambil status dari event (DIPINJAM atau DIKEMBALIKAN/DIKEMBALIKAN_SUKSES)
            String statusEvent = event.getStatus();

            if ("DIPINJAM".equalsIgnoreCase(statusEvent)) {
                // Jika dari Service Peminjaman
                buku.setStatus("Dipinjam");
                log.info("AUDIT_LOG: Status Buku ID {} berhasil di-update ke 'Dipinjam'", buku.getId());
            } else {
                // Jika dari Service Pengembalian (Status biasanya "DIKEMBALIKAN" atau lainnya)
                buku.setStatus("Tersedia");
                log.info("AUDIT_LOG: Status Buku ID {} berhasil di-update ke 'Tersedia'", buku.getId());
            }

            // Simpan perubahan ke database buku
            bukuRepository.save(buku);
            
        }, () -> {
            log.error("AUDIT_LOG: Gagal update, Buku dengan ID {} tidak ditemukan di database", event.getIdBuku());
        });
    }
}