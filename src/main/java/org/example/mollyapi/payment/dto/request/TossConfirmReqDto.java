package org.example.mollyapi.payment.dto.request;

import lombok.Builder;

@Builder
public record TossConfirmReqDto(
        String paymentKey,
        String orderId,
        Long amount
) {
}
