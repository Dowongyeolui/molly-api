package org.example.mollyapi.common.exception.error.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.mollyapi.common.exception.error.CustomError;
import org.springframework.http.HttpStatus;

import java.util.Arrays;


@Getter
@AllArgsConstructor
public enum PaymentError implements CustomError {

    // 결제 서버 에러
    INVALID_PAYMENT_REQUEST("잘못된 결제 요청입니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_NOT_FOUND("결제 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PAYMENT_ALREADY_PROCESSED("이미 처리된 결제입니다.", HttpStatus.CONFLICT),
    PAYMENT_FAILED("결제 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_GATEWAY_ERROR("결제 게이트웨이 오류입니다.", HttpStatus.BAD_GATEWAY),
    PAYMENT_AMOUNT_MISMATCH("결제 금액이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_POINT_INSUFFICIENT("보유한 포인트가 충분하지 않습니다.", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND("주문 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PAYMENT_RETRY_EXCEEDED("결제 시도 횟수가 초과되었습니다.", HttpStatus.CONFLICT),
    ORDER_EXPIRED("주문 기한이 만료되었습니다.", HttpStatus.BAD_REQUEST),


    // 결제 취소 에러
    PAYMENT_ALREADY_CANCELED("이미 취소된 결제입니다.", HttpStatus.CONFLICT),

    ;

    private final String message;
    private final HttpStatus status;

    public static PaymentError from(HttpStatus status) {
        return Arrays.stream(values())
                .filter(e -> e.status == status)
                .findFirst()
                .orElse(PAYMENT_FAILED); // 기본값
    }
}

