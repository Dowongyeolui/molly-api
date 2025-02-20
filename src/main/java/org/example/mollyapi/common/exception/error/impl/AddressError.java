package org.example.mollyapi.common.exception.error.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.mollyapi.common.exception.error.CustomError;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AddressError implements CustomError {
    ADDRESS_DELETE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "ADDRESS_001", "기본 주소는 삭제할 수 없습니다."),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "ADDRESS_002", "해당 주소를 찾을 수 없습니다."),
    ADDRESS_ALREADY_DEFAULT(HttpStatus.CONFLICT, "ADDRESS_003", "이미 기본 주소로 설정되어 있습니다."),
    ADDRESS_UPDATE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "ADDRESS_004", "해당 주소는 수정할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}