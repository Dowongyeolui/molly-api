package org.example.mollyapi.common.exception.error.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.mollyapi.common.exception.error.CustomError;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProductItemError implements CustomError {

    NOT_EXISTS_PRODUCT(HttpStatus.BAD_REQUEST, "해당 상품이 존재하지 않습니다."),
    NOT_EXISTS_ITEM(HttpStatus.BAD_REQUEST, "해당 상품의 컬러&사이즈가 존재하지 않습니다."),
    OVER_QUANTITY(HttpStatus.BAD_REQUEST, "준비된 수량을 초과했습니다."),
    SOLD_OUT(HttpStatus.BAD_REQUEST, "해당 상품은 품절입니다.")
    ;


    private final HttpStatus status;
    private final String message;

}
