package org.example.mollyapi.payment.repository;

import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.payment.entity.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentKey(String paymentKey);

    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId ORDER BY p.paymentDate DESC")
    List<Payment> findLatestPaymentByOrderId(@Param("orderId") Long orderId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId")
    Optional<List<Payment>> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM Payment p WHERE p.tossOrderId = :tossOrderId ORDER BY p.paymentDate DESC")
    Optional<Payment> findTopLatestPaymentByOrderId(@Param("tossOrderId") String tossOrderId);
}
