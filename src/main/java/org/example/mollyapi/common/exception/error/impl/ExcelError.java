package org.example.mollyapi.common.exception.error.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.mollyapi.common.exception.error.CustomError;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ExcelError implements CustomError {

    INVALID_CELL_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 CELL 타입입니다."),
    ;

    private final HttpStatus status;
    private final String message;
}
