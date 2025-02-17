package org.example.mollyapi.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record PaymentInfoReqDto (
        @Schema(description = "주문 고유 ID", example = "1")
        Long orderId
){
}
