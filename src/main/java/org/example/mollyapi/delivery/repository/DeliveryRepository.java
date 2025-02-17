package org.example.mollyapi.delivery.repository;

import org.example.mollyapi.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByOrderId(Long orderId);
}