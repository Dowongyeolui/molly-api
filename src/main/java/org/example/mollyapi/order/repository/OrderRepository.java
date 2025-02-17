package org.example.mollyapi.order.repository;

import org.example.mollyapi.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findById(Long orderId);
    Optional<Order> findByTossOrderId(String tossOrderId);

    @Query("SELECT COUNT(od) FROM OrderDetail od WHERE od.order.tossOrderId = :tossOrderId")
    int countOrderDetailsByTossOrderId(@Param("tossOrderId") String tossOrderId);

}