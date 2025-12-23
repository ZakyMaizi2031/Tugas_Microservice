package com.zaky.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderProducerService {

    private final RabbitTemplate rabbitTemplate;
    private final OrderRepository orderRepository;
    
    @Value("${app.rabbitmq.exchange}")
    private String exchange;
    
    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    public OrderProducerService(RabbitTemplate rabbitTemplate, OrderRepository orderRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order createAndSendOrder(Order order) {
        // Simpan order ke database
        Order savedOrder = orderRepository.save(order);
        System.out.println("Order saved to database: " + savedOrder);
        
        // Kirim ke RabbitMQ
        rabbitTemplate.convertAndSend(exchange, routingKey, savedOrder);
        System.out.println("Order sent to RabbitMQ: " + savedOrder);
        
        return savedOrder;
    }
}