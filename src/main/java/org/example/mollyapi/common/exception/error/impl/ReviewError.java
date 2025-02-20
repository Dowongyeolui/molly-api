package org.example.mollyapi.common.exception.error.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.mollyapi.common.exception.error.CustomError;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReviewError implements CustomError {

    NOT_ACCESS_REVIEW(HttpStatus.BAD_REQUEST, "리뷰를 작성할 수 있는 권한이 없습니다."),
    NOT_EXIST_REVIEW(HttpStatus.NO_CONTENT, "해당 상품의 리뷰가 존재하지 않습니다."),
    NOT_CHANGED(HttpStatus.NO_CONTENT, "변경된 내역이 없습니다."),
    FAIL_UPDATE(HttpStatus.INTERNAL_SERVER_ERROR, "리뷰 변경에 실패했습니다.")
    ;


    private final HttpStatus status;
    private final String message;

}
