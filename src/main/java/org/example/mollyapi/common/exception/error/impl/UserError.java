package org.example.mollyapi.common.exception.error.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.mollyapi.common.exception.error.CustomError;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserError implements CustomError {

    ALREADY_EXISTS_NICKNAME(HttpStatus.BAD_REQUEST, "중복되는 닉네임입니다."),
    NOT_EXISTS_USER(HttpStatus.BAD_REQUEST, "없는 사용자 입니다.")
    ;


    private final HttpStatus status;
    private final String message;

}
