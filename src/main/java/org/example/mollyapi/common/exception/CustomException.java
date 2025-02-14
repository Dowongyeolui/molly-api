package org.example.mollyapi.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.common.exception.error.CustomError;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Slf4j
@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException{

    private HttpStatus httpStatus;
    private String message;

    public CustomException(CustomError customError) {
        super(customError.getMessage());
        this.message = customError.getMessage();
        this.httpStatus = customError.getStatus();

    }
    public CustomException(HttpStatusCode httpStatus, String message) {
        super(message);
        this.httpStatus = HttpStatus.valueOf(httpStatus.value());
        this.message = message;
    }
}
