package org.example.mollyapi.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.example.mollyapi.payment.entity.Payment;
import org.example.mollyapi.payment.type.PaymentStatus;

import java.time.format.DateTimeFormatter;

public record PaymentInfoResDto (
        @Schema(description = "결제 ID", example = "10")
        Long paymentId,
        @Schema(description = "결제 타입", example = "NORMAL")
        String paymentType,
        @Schema(description = "결제 금액", example = "286000")
        Long amount,
        @Schema(description = "사용한 포인트", example = "0")
        Integer point,
        @Schema(description = "결제 상태", example = "APPROVED")
        PaymentStatus paymentStatus,
        @Schema(description = "실패 사유", example = "잔액 부족")
        String failureReason,
        @Schema(description = "결제 수단", example = "CreditCard")
        String paymentMethod,
        @Schema(description = "결제 완료 시간", example = "2024-02-03 12:31:00")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        String paymentDate
){
    public static PaymentInfoResDto from(Payment payment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedPaymentDate = payment.getPaymentDate() != null ? payment.getPaymentDate().format(formatter) : null;

        return new PaymentInfoResDto(
                payment.getId(),
                payment.getPaymentType(),
                payment.getAmount(),
                payment.getPoint(),
                payment.getPaymentStatus(),
                payment.getFailureReason(),
                payment.getPaymentType(),
                formattedPaymentDate
        );
    }
}