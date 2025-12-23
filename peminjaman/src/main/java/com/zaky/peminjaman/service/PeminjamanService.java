package com.zaky.peminjaman.service;

import java.util.List;
import java.util.Optional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.zaky.peminjaman.config.RabbitMQConfig;
import com.zaky.peminjaman.model.Peminjaman;
import com.zaky.peminjaman.repository.PeminjamanRepository;

@Service
public class PeminjamanService {

    private final PeminjamanRepository peminjamanRepository;
    private final RabbitTemplate rabbitTemplate;

    public PeminjamanService(PeminjamanRepository peminjamanRepository,
                             RabbitTemplate rabbitTemplate) {
        this.peminjamanRepository = peminjamanRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Simpan data peminjaman dan kirim payload ke RabbitMQ
     * Payload format: "anggotaId|bukuId"
     */
    public Peminjaman createPeminjaman(Peminjaman peminjaman) {
        if (peminjaman == null) {
            throw new IllegalArgumentException("Peminjaman tidak boleh null");
        }

        // Simpan data peminjaman ke database
        Peminjaman saved = peminjamanRepository.save(peminjaman);

        // Siapkan payload untuk dikirim ke email-service
        String payload = saved.getAnggotaId() + "|" + saved.getBukuId();

        // Kirim ke RabbitMQ
        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE, payload);

        return saved;
    }

    /**
     * Ambil semua data peminjaman
     */
    public List<Peminjaman> getAllPeminjamans() {
        return peminjamanRepository.findAll();
    }

    /**
     * Ambil peminjaman berdasarkan ID
     */
    public Optional<Peminjaman> getPeminjamanById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return peminjamanRepository.findById(id);
    }

    /**
     * Hapus peminjaman berdasarkan ID
     */
    public void deletePeminjaman(Long id) {
        if (id != null && peminjamanRepository.existsById(id)) {
            peminjamanRepository.deleteById(id);
        }
    }
}
