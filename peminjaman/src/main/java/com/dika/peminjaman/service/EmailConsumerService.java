package com.dika.peminjaman.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.dika.peminjaman.config.RabbitMQConfig;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
class AnggotaDTO {
    private Long id;
    private String nama;
    private String email;
}

@Data
class BukuDTO {
    private Long id;
    private String judul;
}

@Service
@Slf4j
public class EmailConsumerService {

    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate;

    public EmailConsumerService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        this.restTemplate = new RestTemplate();
    }

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void receiveEvent(String payload) {
        try {
            log.info("EMAIL_PROCESSOR: Payload diterima -> {}", payload);
            String[] parts = payload.split("\\|");
            Long anggotaId = Long.parseLong(parts[0]);
            Long bukuId = Long.parseLong(parts[1]);

            AnggotaDTO anggota = restTemplate.getForObject("http://localhost:8082/api/anggota/" + anggotaId, AnggotaDTO.class);
            BukuDTO buku = restTemplate.getForObject("http://localhost:8081/api/buku/" + bukuId, BukuDTO.class);

            if (anggota != null && buku != null && anggota.getEmail() != null) {
                sendEmail(anggota.getEmail(), "Peminjaman Berhasil", 
                    "Halo " + anggota.getNama() + ", Anda berhasil meminjam buku: " + buku.getJudul());
            }
        } catch (Exception e) {
            log.error("Gagal mengirim email: {}", e.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        mailSender.send(message);
        log.info("✅ Email Terkirim ke: {}", to);
    }
}