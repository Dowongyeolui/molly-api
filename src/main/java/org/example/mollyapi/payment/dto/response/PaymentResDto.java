package org.example.mollyapi.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.example.mollyapi.payment.entity.Payment;

import java.time.LocalDateTime;

public record PaymentResDto(
        Long paymentId,
        String paymentType,
        Long amount,
//        LocalDateTime paymentDate,
        String paymentStatus,
        String tossOrderId

) { public static PaymentResDto from(Payment payment) {
    return new PaymentResDto(
            payment.getId(),
            payment.getPaymentType(),
            payment.getAmount(),
//            payment.getPaymentDate(),
            payment.getPaymentStatus().getStatus(),
            payment.getTossOrderId()
            );
}

}
