package org.example.mollyapi.common.exception.error.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.mollyapi.common.exception.error.CustomError;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthError implements CustomError {

    ALREADY_EXISTS_AUTH(HttpStatus.BAD_REQUEST, "이미 존재하는 회원입니다."),
    NOT_MATCH_AUTH(HttpStatus.FORBIDDEN, "비밀번호 혹은 아이디가 일치하지 않습니다."),
    WRONG_APPROACH(HttpStatus.UNAUTHORIZED, "잘못된 접근입니다.")
    ;


    private final HttpStatus status;
    private final String message;

}
