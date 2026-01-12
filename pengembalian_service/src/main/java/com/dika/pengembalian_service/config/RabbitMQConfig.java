package com.dika.pengembalian_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "x.perpustakaan";

    @Bean public TopicExchange exchange() { return new TopicExchange(EXCHANGE); }

    @Bean public Queue qSync() { return new Queue("q.pengembalian.sync"); }
    @Bean public Queue qDel() { return new Queue("q.pengembalian.delete"); }

    @Bean public Binding bSync(Queue qSync, TopicExchange exchange) { 
        return BindingBuilder.bind(qSync).to(exchange).with("pengembalian.sync"); 
    }
    @Bean public Binding bDel(Queue qDel, TopicExchange exchange) { 
        return BindingBuilder.bind(qDel).to(exchange).with("pengembalian.delete"); 
    }

    @Bean public Jackson2JsonMessageConverter jsonConverter() { return new Jackson2JsonMessageConverter(); }
}