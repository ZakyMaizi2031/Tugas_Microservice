package com.zaky.rabbitmq;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByStatus(Order.OrderStatus status);
    List<Order> findByCustomerEmail(String customerEmail);
}