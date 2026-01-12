package com.dika.anggota.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "x.perpustakaan";
    public static final String SYNC_QUEUE = "q.anggota.sync";
    public static final String DELETE_QUEUE = "q.anggota.delete";
    public static final String UPDATE_STATUS_QUEUE = "q.anggota.update-status"; // Tambahkan ini

    @Bean public TopicExchange exchange() { return new TopicExchange(EXCHANGE); }

    // Definisi Antrean
    @Bean public Queue qSync() { return new Queue(SYNC_QUEUE, true); }
    @Bean public Queue qDel() { return new Queue(DELETE_QUEUE, true); }
    @Bean public Queue qUpdateStatus() { return new Queue(UPDATE_STATUS_QUEUE, true); } // Tambahkan ini

    // Binding (Menghubungkan antrean ke Exchange)
    @Bean public Binding bSync(Queue qSync, TopicExchange exchange) { 
        return BindingBuilder.bind(qSync).to(exchange).with("anggota.sync"); 
    }
    @Bean public Binding bDel(Queue qDel, TopicExchange exchange) { 
        return BindingBuilder.bind(qDel).to(exchange).with("anggota.delete"); 
    }
    @Bean public Binding bUpdateStatus(Queue qUpdateStatus, TopicExchange exchange) { 
        return BindingBuilder.bind(qUpdateStatus).to(exchange).with("anggota.update.status"); 
    }

    @Bean public Jackson2JsonMessageConverter jsonConverter() { 
        return new Jackson2JsonMessageConverter(); 
    }
}