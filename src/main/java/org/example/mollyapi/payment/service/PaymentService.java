package org.example.mollyapi.payment.service;

import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.payment.dto.request.PaymentCancelReqDto;
import org.example.mollyapi.payment.dto.request.PaymentRequestDto;
import org.example.mollyapi.payment.dto.request.TossConfirmReqDto;
import org.example.mollyapi.payment.dto.response.PaymentInfoResDto;
import org.example.mollyapi.payment.dto.response.TossConfirmResDto;
import org.example.mollyapi.payment.entity.Payment;
import org.example.mollyapi.payment.type.PaymentStatus;
import org.example.mollyapi.user.entity.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface PaymentService {

    //결제 승인 절차
    Payment processPayment(Long userId, PaymentRequestDto requestDto);

    
    public Payment processPayment(User user, Order order, PaymentRequestDto requestDto);

//    //결제 성공 절차
//    public void successPayment(Payment payment, String tossOrderId,Integer point, String deliveryInfoJson);

//    //결제 실패 절차
//    public void failPayment(Payment payment, String tossOrderId, String failureReason);

    //결제 취소 절차
    public boolean cancelPayment(Long userId, PaymentCancelReqDto paymentCancelReqDto, PaymentStatus paymentStatus);

    //PaymentKey로 결제찾기
    public Payment findPaymentByPaymentKey(String paymentKey);

    //orderId로 최신결제찾기
    public PaymentInfoResDto findLatestPayment(Long orderId);

    //orderId로 모든 결제정보 찾기
    public List<PaymentInfoResDto> findAllPayments(Long orderId);

    //userId로 모든 결제정보 찾기
    public List<PaymentInfoResDto> findUserPayments(Long userId);

    public Payment createPayment(Long userId, String tossOrderId, String paymentKey, String paymentType, Long amount, PaymentStatus paymentStatus);

    <T> boolean validateResponse(ResponseEntity<T> response);

    ResponseEntity<TossConfirmResDto> tossPaymentApi(TossConfirmReqDto tossConfirmReqDto);

    Payment retryPayment(Long userId, String tossOrderId, String paymentKey);
}
