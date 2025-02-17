package org.example.mollyapi.common.exception.error.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.mollyapi.common.exception.error.CustomError;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OrderDetailError
        implements CustomError {

    NOT_EXIST_ORDERDETIAL(HttpStatus.BAD_REQUEST, "해당 주문 내역을 조회할 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String message;

}
