package org.example.mollyapi.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

public record TossCancelResDto(
        String mid,
        String transactionKey,
        String paymentKey,
        String tossOrderId,
        String orderName,
        String method,
        String status,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Cancel cancel
) {
    public record Cancel(
            String cancelReason,
            LocalDateTime canceledAt,
            Long cancelAmount,
            Long refundableAmount,
            String transactionKey,
            String receiptKey,
            String cancelStatus
    ) {
    }
}
