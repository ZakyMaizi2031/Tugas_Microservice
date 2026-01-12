package com.dika.anggota.cqrs.command.handler;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; // Import Lombok Slf4j
import org.springframework.transaction.annotation.Transactional;

import com.dika.anggota.cqrs.command.model.Anggota;
import com.dika.anggota.repository.AnggotaJpaRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j // Aktifkan logging
public class AnggotaCommandHandler {

    @Autowired private AnggotaJpaRepository jpaRepo;
    @Autowired private RabbitTemplate rabbitTemplate;

    @Transactional
    public Anggota handleCreate(Anggota anggota) {
        log.info("[COMMAND] Memproses pendaftaran anggota baru: NIM={}, Nama={}", anggota.getNim(), anggota.getNama());
        
        Anggota saved = jpaRepo.save(anggota);
        log.info("[COMMAND] Berhasil simpan ke MySQL. ID Tergenerate: {}", saved.getId());

        // Kirim sinyal sync ke RabbitMQ
        log.info("[SYNC] Mengirim data Anggota ID {} ke RabbitMQ untuk sinkronisasi MongoDB", saved.getId());
        rabbitTemplate.convertAndSend("x.perpustakaan", "anggota.sync", saved);
        
        return saved;
    }

    @Transactional
    public Anggota handleUpdate(Long id, Anggota anggotaReq) {
        log.info("[COMMAND] Memproses update data untuk Anggota ID: {}", id);

        return jpaRepo.findById(id).map(anggota -> {
            anggota.setNim(anggotaReq.getNim());
            anggota.setNama(anggotaReq.getNama());
            anggota.setEmail(anggotaReq.getEmail());
            anggota.setAlamat(anggotaReq.getAlamat());
            anggota.setJenis_kelamin(anggotaReq.getJenis_kelamin());
            
            Anggota updated = jpaRepo.save(anggota);
            log.info("[COMMAND] Update MySQL sukses untuk Anggota ID: {}", id);

            log.info("[SYNC] Memicu sinkronisasi update ke MongoDB untuk Anggota ID: {}", id);
            rabbitTemplate.convertAndSend("x.perpustakaan", "anggota.sync", updated);
            
            return updated;
        }).orElseThrow(() -> {
            log.error("[COMMAND_ERROR] Gagal update. Anggota ID {} tidak ditemukan di MySQL", id);
            return new RuntimeException("Anggota tidak ditemukan");
        });
    }

    @Transactional
    public void handleDelete(Long id) {
        log.info("[COMMAND] Memproses penghapusan Anggota ID: {}", id);

        if(jpaRepo.existsById(id)) {
            jpaRepo.deleteById(id);
            log.info("[COMMAND] Berhasil menghapus Anggota ID {} dari MySQL", id);

            log.info("[SYNC] Mengirim sinyal hapus ke RabbitMQ untuk Anggota ID: {}", id);
            rabbitTemplate.convertAndSend("x.perpustakaan", "anggota.delete", id);
        } else {
            log.warn("[COMMAND_WARN] Percobaan hapus gagal. Anggota ID {} memang tidak ada di DB", id);
        }
    }
}