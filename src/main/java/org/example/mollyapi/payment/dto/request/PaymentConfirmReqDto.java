package org.example.mollyapi.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;


@Builder
public record PaymentConfirmReqDto(

        @Schema(description = "토스 주문 ID", example = "ORD-20250213132349-6572")
        String tossOrderId,
        @Schema(description = "토스 결제 KEY", example = "tgen_202502131548483DSE4")
        String paymentKey,
        @Schema(description = "결제금액", example = "258000")
        Long amount,
        @Schema(description = "결제수단", example = "NORMAL")
        String paymentType,
        @Schema(description = "사용한 포인트", example = "0")
        Integer point,
//        @Schema(description = "배송지 id", example = "1")
        Long deliveryId
)
{}