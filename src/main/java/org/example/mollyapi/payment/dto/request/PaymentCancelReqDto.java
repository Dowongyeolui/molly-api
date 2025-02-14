package org.example.mollyapi.payment.dto.request;

public record PaymentCancelReqDto (
        String paymentKey,
        String cancelReason,
        Long cancelAmount
//        String refundReceiveAccount
){
}
