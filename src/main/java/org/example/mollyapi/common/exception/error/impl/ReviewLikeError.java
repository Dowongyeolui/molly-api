package org.example.mollyapi.common.exception.error.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.mollyapi.common.exception.error.CustomError;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReviewLikeError implements CustomError {

    EXIST_LIKE(HttpStatus.BAD_REQUEST, "리뷰 좋아요가 이미 존재합니다."),
    NOT_EXIST_LIKE(HttpStatus.BAD_REQUEST, "리뷰 좋아요가 존재하지 않습니다.")
    ;

    private final HttpStatus status;
    private final String message;

}