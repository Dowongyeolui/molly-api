package org.example.mollyapi.payment.repository;

import org.example.mollyapi.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentKey(String paymentKey);
    Optional<Payment> findByTossOrderId(String tossOrderId);
}
