package org.example.mollyapi.common.exception.error.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.mollyapi.common.exception.error.CustomError;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CategoryError implements CustomError {
    NOT_EXIST_CATEGORY(HttpStatus.BAD_REQUEST, "존재하지 않는 카테고리입니다"),
    ;

    private final HttpStatus status;
    private final String message;
}
