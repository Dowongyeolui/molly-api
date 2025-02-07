package org.example.mollyapi.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CustomErrorResponse> handleCustomException(CustomException e){
        return CustomErrorResponse.toResponseEntity(e);
    }

    /**
     * (@Valid - Not Null. Not Blank. Email.. Password 정규패턴 등등) 에 대한 오류 핸들러
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException e){
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", "NOT_PARAM_VALID");
        e.getBindingResult().getAllErrors()
                .forEach(c -> errors.put(((FieldError) c).getField(), c.getDefaultMessage()));
        errors.put("httpStatusCode", HttpStatus.BAD_REQUEST);
        return ResponseEntity.badRequest().body(errors);
    }
}
