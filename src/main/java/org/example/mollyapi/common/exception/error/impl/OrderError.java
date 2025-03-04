package org.example.mollyapi.common.exception.error.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.mollyapi.common.exception.error.CustomError;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OrderError implements CustomError {

    ORDER_WITHDRAW_REFUND_FAIL(HttpStatus.BAD_REQUEST, "포인트 환불 중 오류가 발생했습니다. 관리자에게 문의하세요."),
    PAYMENT_RETRY_REQUIRED(HttpStatus.PAYMENT_REQUIRED, "결제가 실패했습니다. 다시 시도하시겠습니까? (API: /orders/{orderId}/retry-payment)")
    ;

    private final HttpStatus status;
    private final String message;
}
