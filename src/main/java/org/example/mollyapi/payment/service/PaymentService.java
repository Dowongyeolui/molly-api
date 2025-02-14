package org.example.mollyapi.payment.service;

import org.example.mollyapi.payment.dto.request.PaymentCancelReqDto;
import org.example.mollyapi.payment.dto.response.PaymentResDto;
import org.example.mollyapi.payment.entity.Payment;

import java.util.List;

public interface PaymentService {

    //결제 승인 절차
    public Payment processPayment(Long userId, String paymentKey, String tossOrderId, Long amount, Integer point, Long deliveryId, String paymentType);

    //결제 성공 절차
    public void successPayment(Payment payment, String tossOrderId,Integer point);

    //결제 실패 절차
    public void failPayment(Payment payment, String tossOrderId, String failureReason);

    //결제 취소 절차
    public boolean cancelPayment(Long userId, PaymentCancelReqDto paymentCancelReqDto);
//
    //PaymentKey로 결제찾기
    public Payment findPaymentByPaymentKey(String paymentKey);


}
