package com.zaky.peminjaman.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.zaky.peminjaman.config.RabbitMQConfig;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

// DTO minimal untuk anggota
class AnggotaDTO {
    private Long id;
    private String nama;
    private String email;
    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

// DTO minimal untuk buku
class BukuDTO {
    private Long id;
    private String judul;
    private String pengarang;
    private String penerbit;
    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getJudul() { return judul; }
    public void setJudul(String judul) { this.judul = judul; }
    public String getPengarang() { return pengarang; }
    public void setPengarang(String pengarang) { this.pengarang = pengarang; }
    public String getPenerbit() { return penerbit; }
    public void setPenerbit(String penerbit) { this.penerbit = penerbit; }
}

@Service
public class EmailConsumerService {

    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate;

    private final String anggotaServiceUrl = "http://localhost:8082/api/anggota/";
    private final String bukuServiceUrl = "http://localhost:8081/api/buku/";

    public EmailConsumerService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Listener RabbitMQ
     * Format payload: "anggotaId|bukuId"
     */
    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void receiveEvent(String payload) {
        try {
            String[] parts = payload.split("\\|");
            Long anggotaId = Long.parseLong(parts[0]);
            Long bukuId = Long.parseLong(parts[1]);

            // Ambil data anggota
            ResponseEntity<AnggotaDTO> responseAnggota =
                    restTemplate.getForEntity(anggotaServiceUrl + anggotaId, AnggotaDTO.class);
            AnggotaDTO anggota = responseAnggota.getBody();

            // Ambil data buku
            ResponseEntity<BukuDTO> responseBuku =
                    restTemplate.getForEntity(bukuServiceUrl + bukuId, BukuDTO.class);
            BukuDTO buku = responseBuku.getBody();

            // Kirim email jika data valid
            if (anggota != null && buku != null && anggota.getEmail() != null) {
                String emailBody = "<h2>Peminjaman Buku Berhasil</h2>"
                        + "<p>Halo <strong>" + anggota.getNama() + "</strong>,</p>"
                        + "<p>Anda telah berhasil meminjam buku dengan detail berikut:</p>"
                        + "<table border='1' cellpadding='5' cellspacing='0'>"
                        + "<tr><th>Judul</th><td>" + buku.getJudul() + "</td></tr>"
                        + "<tr><th>Pengarang</th><td>" + buku.getPengarang() + "</td></tr>"
                        + "<tr><th>Penerbit</th><td>" + buku.getPenerbit() + "</td></tr>"
                        + "</table>"
                        + "<p>Terima kasih telah menggunakan layanan kami.</p>";

                sendEmail(anggota.getEmail(), "Peminjaman Buku Berhasil", emailBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true = HTML
            mailSender.send(message);
            System.out.println("✅ Email terkirim ke " + to);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
