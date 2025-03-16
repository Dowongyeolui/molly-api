package org.example.mollyapi.payment.service;

import org.example.mollyapi.payment.dto.request.PaymentCancelReqDto;
import org.example.mollyapi.payment.dto.request.PaymentConfirmReqDto;
import org.example.mollyapi.payment.dto.request.TossConfirmReqDto;
import org.example.mollyapi.payment.dto.response.PaymentInfoResDto;
import org.example.mollyapi.payment.dto.response.TossConfirmResDto;
import org.example.mollyapi.payment.entity.Payment;
import org.example.mollyapi.payment.type.PaymentStatus;
import org.example.mollyapi.user.entity.User;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface PaymentService {

    //결제 승인 절차
    Payment processPayment(Long userId, PaymentConfirmReqDto requestDto);

    Payment processPaymentTest(Long userId, PaymentConfirmReqDto requestDto, String status);

//    public Payment processPayment(User user, Order order, PaymentRequestDto requestDto);

//    //결제 성공 절차
//    public void successPayment(Payment payment, String tossOrderId,Integer point, String deliveryInfoJson);

//    //결제 실패 절차
//    public void failPayment(Payment payment, String tossOrderId, String failureReason);

    //결제 취소 절차
    public boolean cancelPayment(Long userId, PaymentCancelReqDto paymentCancelReqDto, PaymentStatus paymentStatus);

    //PaymentKey로 결제찾기
    public Payment findPaymentByPaymentKey(String paymentKey);

    //orderId로 최신결제찾기
    public Optional<PaymentInfoResDto> findLatestPayment(Long orderId);


    //userId로 모든 결제정보 찾기
    public List<PaymentInfoResDto> findUserPayments(Long userId);

    public Payment createOrGetPayment(Long userId, Long orderId, String tossOrderId, String paymentKey, String paymentType, Long amount);

    Payment retryPayment(Long userId, String tossOrderId, String paymentKey);
}
