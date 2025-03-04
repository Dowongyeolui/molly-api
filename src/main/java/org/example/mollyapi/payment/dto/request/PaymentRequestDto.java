package org.example.mollyapi.payment.dto.request;

import org.example.mollyapi.delivery.dto.DeliveryReqDto;

public record PaymentRequestDto(
        String tossOrderId,
        String paymentKey,
        Long amount,
        String paymentType,
        String point,
        DeliveryReqDto delivery
) {}