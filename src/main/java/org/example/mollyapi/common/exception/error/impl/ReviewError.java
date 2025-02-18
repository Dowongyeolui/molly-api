package org.example.mollyapi.common.exception.error.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.mollyapi.common.exception.error.CustomError;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReviewError implements CustomError {

    NOT_ACCESS_REVIEW(HttpStatus.BAD_REQUEST, "리뷰를 작성할 수 있는 권한이 없습니다."),
    NOT_EXIST_REVIEW(HttpStatus.NO_CONTENT, "해당 상품의 리뷰가 존재하지 않습니다.")
    ;


    private final HttpStatus status;
    private final String message;

}
