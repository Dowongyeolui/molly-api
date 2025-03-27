package org.example.mollyapi.payment.exception;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@AllArgsConstructor
public class RetryablePaymentException extends RuntimeException {
    public RetryablePaymentException(String message) {
        super(message);
    }
}
