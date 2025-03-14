package org.example.mollyapi.common.exception;

import org.example.mollyapi.payment.exception.RetryablePaymentException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

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

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body("파일 크기가 너무 큽니다! 최대 업로드 크기를 확인하세요.");
    }

    @ExceptionHandler(RetryablePaymentException.class)
    public ResponseEntity<String> handleRetryablePaymentException(RetryablePaymentException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ex.getMessage());
    }

}
