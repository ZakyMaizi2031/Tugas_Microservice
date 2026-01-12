package com.dika.buku.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "x.perpustakaan";
    public static final String SYNC_QUEUE = "q.buku.sync";
    public static final String UPDATE_STATUS_QUEUE = "q.buku.update-status";
    public static final String DELETE_QUEUE = "q.buku.delete";

    @Bean public TopicExchange exchange() { return new TopicExchange(EXCHANGE); }

    @Bean public Queue qSync() { return new Queue(SYNC_QUEUE); }
    @Bean public Queue qUpdate() { return new Queue(UPDATE_STATUS_QUEUE); }
    @Bean public Queue qDel() { return new Queue(DELETE_QUEUE); }

    // Binding Sinkronisasi Internal (MySQL -> Mongo)
    @Bean public Binding bSync(Queue qSync, TopicExchange exchange) { 
        return BindingBuilder.bind(qSync).to(exchange).with("buku.sync"); 
    }

    // Binding Sinkronisasi Eksternal (Dari Service Peminjaman/Pengembalian)
    @Bean public Binding bUpdate(Queue qUpdate, TopicExchange exchange) { 
        return BindingBuilder.bind(qUpdate).to(exchange).with("buku.update.status"); 
    }

    @Bean public Binding bDel(Queue qDel, TopicExchange exchange) { 
        return BindingBuilder.bind(qDel).to(exchange).with("buku.delete"); 
    }

    @Bean public Jackson2JsonMessageConverter jsonConverter() { return new Jackson2JsonMessageConverter(); }
}