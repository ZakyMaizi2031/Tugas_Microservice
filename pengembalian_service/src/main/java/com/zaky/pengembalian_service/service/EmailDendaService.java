package com.zaky.pengembalian_service.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailDendaService {

    private final JavaMailSender mailSender;

    public EmailDendaService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void kirimEmailDenda(String email, String nama, String judulBuku, long hariTerlambat, double totalDenda) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);
            helper.setSubject("Pemberitahuan Denda Keterlambatan Buku");

            String htmlMsg = "<html><body>" +
                    "<p>Halo <b>" + nama + "</b>,</p>" +
                    "<p>Anda terlambat mengembalikan buku: <b>" + judulBuku + "</b></p>" +
                    "<p>Hari Terlambat: " + hariTerlambat + " hari<br>" +
                    "Total Denda: Rp " + String.format("%,.0f", totalDenda) + "</p>" +
                    "<p>Mohon segera membayar denda.</p>" +
                    "</body></html>";

            helper.setText(htmlMsg, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}

