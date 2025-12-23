package com.zaky.rabbitmq;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final OrderProducerService orderProducerService;

    public DataInitializer(OrderProducerService orderProducerService) {
        this.orderProducerService = orderProducerService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Data sample untuk testing
        Order order1 = new Order("Laptop", 1, 15000000.0, "mzaky0902@gmail.com");
        Order order2 = new Order("Mouse", 2, 250000.0, "mzakymaizi80@gmail.com");
        Order order3 = new Order("Keyboard", 1, 500000.0, "mzakym707@gmail.com");
        
        orderProducerService.createAndSendOrder(order1);
        orderProducerService.createAndSendOrder(order2);
        orderProducerService.createAndSendOrder(order3);
        
        System.out.println("Sample data initialized");
    }
}