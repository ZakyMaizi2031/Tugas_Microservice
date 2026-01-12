package com.dika.peminjaman.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // Tambahkan konstanta ini agar bisa dipanggil di file lain
    public static final String EXCHANGE = "x.perpustakaan";
    public static final String EMAIL_QUEUE = "email-queue";
    public static final String SYNC_QUEUE = "q.peminjaman.sync";
    public static final String DELETE_QUEUE = "q.peminjaman.delete";
    public static final String STATUS_QUEUE = "q.peminjaman.update-status"; // Untuk menerima dari Pengembalian

    @Bean public TopicExchange exchange() { return new TopicExchange(EXCHANGE); }

    @Bean public Queue qSync() { return new Queue(SYNC_QUEUE); }
    @Bean public Queue qDel() { return new Queue(DELETE_QUEUE); }
    @Bean public Queue emailQueue() { return new Queue(EMAIL_QUEUE, true); }
    
    // Tambahkan bean ini agar PeminjamanStatusConsumer bisa jalan
    @Bean public Queue qStatus() { return new Queue(STATUS_QUEUE); }

    @Bean public Binding bSync(Queue qSync, TopicExchange exchange) { 
        return BindingBuilder.bind(qSync).to(exchange).with("peminjaman.sync"); 
    }
    @Bean public Binding bDel(Queue qDel, TopicExchange exchange) { 
        return BindingBuilder.bind(qDel).to(exchange).with("peminjaman.delete"); 
    }
    @Bean public Binding bStatus(Queue qStatus, TopicExchange exchange) { 
        return BindingBuilder.bind(qStatus).to(exchange).with("pengembalian.sukses"); 
    }

    @Bean public Jackson2JsonMessageConverter jsonConverter() { 
        return new Jackson2JsonMessageConverter(); 
    }
}