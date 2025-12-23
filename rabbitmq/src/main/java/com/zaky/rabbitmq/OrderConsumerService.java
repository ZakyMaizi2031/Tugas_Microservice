package com.zaky.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;

@Service
public class OrderConsumerService {

    private final OrderRepository orderRepository;
    private final JavaMailSender mailSender;

    public OrderConsumerService(OrderRepository orderRepository, JavaMailSender mailSender) {
        this.orderRepository = orderRepository;
        this.mailSender = mailSender;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    @Transactional
    public void receiveOrder(@Payload Order order) {
        try {
            System.out.println("Order received from RabbitMQ: " + order);

            // Update status order
            order.setStatus(Order.OrderStatus.PROCESSING);
            orderRepository.save(order);

            // Proses bisnis + kirim email
            processOrder(order);

            // Update status setelah selesai diproses
            order.setStatus(Order.OrderStatus.COMPLETED);
            order.setProcessedAt(java.time.LocalDateTime.now());
            orderRepository.save(order);

            System.out.println("Order processed successfully: " + order.getId());

        } catch (Exception e) {
            System.err.println("Error processing order: " + order.getId() + ", Error: " + e.getMessage());

            // Update status jika gagal
            order.setStatus(Order.OrderStatus.FAILED);
            orderRepository.save(order);

            throw new RuntimeException("Failed to process order", e);
        }
    }

    private void processOrder(Order order) {
        System.out.println("Processing order: " + order.getId());

        // Kirim email ke customer
        try {
            sendEmail(order);
            System.out.println("Email sent to: " + order.getCustomerEmail());
        } catch (Exception e) {
            System.err.println("Failed to send email to: " + order.getCustomerEmail() + " | Error: " + e.getMessage());
        }

        // Simulasi delay processing
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Order processing completed: " + order.getId());
    }

    private void sendEmail(Order order) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(order.getCustomerEmail());
        helper.setSubject("Konfirmasi Pesanan #" + order.getId());

        String htmlContent = "<h2>Halo, terima kasih sudah berbelanja di <b>XXX Store</b>!</h2>"
                + "<p>Detail pesanan Anda:</p>"
                + "<ul>"
                + "<li><b>Produk:</b> " + order.getProductName() + "</li>"
                + "<li><b>Jumlah:</b> " + order.getQuantity() + "</li>"
                + "<li><b>Harga:</b> Rp " + order.getPrice() + "</li>"
                + "<li><b>Status:</b> " + order.getStatus() + "</li>"
                + "</ul>"
                + "<p>Pesanan Anda sedang kami proses dan akan segera dikirim.</p>"
                + "<p><br>Salam hangat,<br><b>Tim Dika Store</b></p>";

        // true = kirim sebagai HTML
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}
