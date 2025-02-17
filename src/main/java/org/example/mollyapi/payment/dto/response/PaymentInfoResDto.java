package org.example.mollyapi.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.example.mollyapi.payment.entity.Payment;
import org.example.mollyapi.payment.type.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentInfoResDto (
        @Schema(description = "결제 타입", example = "NORMAL")
        String paymentType,
        @Schema(description = "결제 금액", example = "286000")
        Long amount,
        @Schema(description = "실패 사유", example = "NORMAL")
        String failureReason,
        @Schema(description = "사용한 포인트", example = "0")
        Integer point,
        @Schema(description = "결제 상태", example = "APPROVED")
        PaymentStatus paymentStatus
){
    public static PaymentInfoResDto from(Payment payment) {
        return new PaymentInfoResDto(
                payment.getPaymentType(),
                payment.getAmount(),
                payment.getFailureReason(),
                payment.getPoint(),
                payment.getPaymentStatus()
        );
    }
}
