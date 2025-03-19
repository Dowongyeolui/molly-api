package org.example.mollyapi.common.exception.error.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.mollyapi.common.exception.error.CustomError;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommonError implements CustomError {

    HEALTH_DISABLE(HttpStatus.BAD_REQUEST, "헬스 체크 API 가 동작하지 않습니다.");


    private final HttpStatus status;
    private final String message;

}
