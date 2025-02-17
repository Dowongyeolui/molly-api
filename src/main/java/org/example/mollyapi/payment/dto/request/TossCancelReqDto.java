package org.example.mollyapi.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record TossCancelReqDto (
        @Schema(description = "주문 취소 사유", example = "단순 변심")
        String cancelReason,
        @Schema(description = "취소 금액", example = "38000")
        Long cancelAmount
//        @Schema(description = "환불 계좌 (가상계좌 결제시)", example = "")
//        String refundReceiveAccount
){

}
