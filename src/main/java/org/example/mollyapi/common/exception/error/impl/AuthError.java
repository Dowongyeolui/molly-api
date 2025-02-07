package org.example.mollyapi.common.exception.error.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.mollyapi.common.exception.error.CustomError;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthError implements CustomError {

    ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 회원입니다.");


    private final HttpStatus status;
    private final String message;

}
