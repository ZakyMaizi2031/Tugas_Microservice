package com.zaky.peminjaman.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.zaky.peminjaman.config.RabbitMQConfig;
import com.zaky.peminjaman.cqrs.event.PeminjamanCreatedEvent;

@Service
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishPeminjamanCreated(PeminjamanCreatedEvent event) {
        String payload = event.getAnggotaId() + "|" + event.getBukuId();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE, payload);
        System.out.println("Event published: " + payload);
    }
}
