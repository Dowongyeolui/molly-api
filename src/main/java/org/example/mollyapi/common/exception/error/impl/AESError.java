package org.example.mollyapi.common.exception.error.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.mollyapi.common.exception.error.CustomError;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AESError implements CustomError {

    DECODE_FAIL(HttpStatus.BAD_REQUEST, "암호화 데이터가 올바르지 않습니다."),
    ;

    private final HttpStatus status;
    private final String message;
}
