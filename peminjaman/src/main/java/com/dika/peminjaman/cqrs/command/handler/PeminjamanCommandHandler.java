package com.dika.peminjaman.cqrs.command.handler;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dika.peminjaman.config.RabbitMQConfig;
import com.dika.peminjaman.cqrs.command.model.Peminjaman;
import com.dika.peminjaman.dto.PengembalianEvent;
import com.dika.peminjaman.repository.PeminjamanJpaRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j // Aktifkan logging profesional
public class PeminjamanCommandHandler {

    @Autowired private PeminjamanJpaRepository jpaRepo;
    @Autowired private RabbitTemplate rabbitTemplate;

    @Transactional
    public Peminjaman handleCreate(Peminjaman peminjaman) {
        log.info("[COMMAND] Memproses peminjaman baru: Anggota ID={}, Buku ID={}", 
                 peminjaman.getAnggotaId(), peminjaman.getBukuId());

        // 1. SIMPAN KE MYSQL (Write Database)
        peminjaman.setStatus("DIPINJAM");
        Peminjaman saved = jpaRepo.save(peminjaman);
        log.info("[COMMAND] Berhasil simpan transaksi ke MySQL. ID Transaksi: {}", saved.getId());

        // 2. SINKRONISASI INTERNAL (MySQL Peminjaman -> MongoDB Peminjaman)
        log.info("[SYNC] Mengirim data transaksi ke MongoDB Peminjaman untuk ID: {}", saved.getId());
        rabbitTemplate.convertAndSend("x.perpustakaan", "peminjaman.sync", saved);

        // 3. SINKRONISASI EKSTERNAL (Update Status Buku ke "Dipinjam")
        log.info("[EXTERNAL] Mengirim instruksi ke Service Buku untuk mengubah status Buku ID: {} menjadi DIPINJAM", saved.getBukuId());
        PengembalianEvent eventBuku = new PengembalianEvent();
        eventBuku.setIdBuku(saved.getBukuId());
        eventBuku.setStatus("DIPINJAM");
        eventBuku.setIdPeminjaman(saved.getId());
        
        // Kirim ke Service Buku via routing key buku.update.status
        rabbitTemplate.convertAndSend("x.perpustakaan", "buku.update.status", eventBuku);

        // 4. KIRIM EMAIL (Side Effect)
        log.info("[NOTIFICATION] Memicu pengiriman email notifikasi peminjaman untuk Anggota ID: {}", saved.getAnggotaId());
        String emailPayload = saved.getAnggotaId() + "|" + saved.getBukuId();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE, emailPayload);

        return saved;
    }

    @Transactional
    public void handleDelete(Long id) {
        log.info("[COMMAND] Memproses penghapusan transaksi peminjaman ID: {}", id);

        if(jpaRepo.existsById(id)) {
            jpaRepo.deleteById(id);
            log.info("[COMMAND] Berhasil menghapus transaksi ID {} dari MySQL", id);

            log.info("[SYNC] Mengirim sinyal hapus ke RabbitMQ untuk MongoDB Peminjaman ID: {}", id);
            rabbitTemplate.convertAndSend("x.perpustakaan", "peminjaman.delete", id);
        } else {
            log.warn("[COMMAND_WARN] Gagal hapus: Transaksi ID {} tidak ditemukan", id);
        }
    }
}