package org.example.mollyapi.payment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.payment.entity.Payment;
import org.example.mollyapi.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class PaymentSaveService {

    private final PaymentRepository paymentRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment persistPayment(Payment payment) {
        return paymentRepository.saveAndFlush(payment);
    }
}
