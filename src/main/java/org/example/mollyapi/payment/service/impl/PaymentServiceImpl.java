package org.example.mollyapi.payment.service.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.common.exception.error.impl.PaymentError;
import org.example.mollyapi.common.exception.error.impl.UserError;
import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.order.repository.OrderRepository;
import org.example.mollyapi.payment.dto.request.*;
import org.example.mollyapi.payment.dto.response.PaymentInfoResDto;
import org.example.mollyapi.payment.dto.response.TossCancelResDto;
import org.example.mollyapi.payment.dto.response.TossConfirmResDto;
import org.example.mollyapi.payment.entity.Payment;
import org.example.mollyapi.payment.repository.PaymentRepository;
import org.example.mollyapi.payment.service.PaymentService;
import org.example.mollyapi.payment.type.PaymentStatus;
import org.example.mollyapi.payment.util.PaymentWebClientUtil;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentWebClientUtil paymentWebClientUtil;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Value("${secret.payment-api-key}")
    private String apiKey;

    @Override
    public Payment findPaymentByPaymentKey(String paymentKey) {
        return paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new CustomException(PaymentError.PAYMENT_NOT_FOUND));
    }

    @Override
    public Optional<PaymentInfoResDto> findLatestPayment(Long orderId) {
        Pageable pageable = PageRequest.of(0, 1);
        return paymentRepository.findLatestPaymentByOrderId(orderId, pageable).stream()
                .findFirst()
                .map(PaymentInfoResDto::from);
    }

    @Override
    public List<PaymentInfoResDto> findUserPayments(Long userId) {
        return paymentRepository.findAllByUserId(userId)
                .orElseThrow(() -> new CustomException(PaymentError.PAYMENT_NOT_FOUND))
                .stream()
                .map(PaymentInfoResDto::from)
                .collect(Collectors.toList());
    }

    /*
        결제 요청 실행 (API 호출 및 결제 데이터 저장)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment processPayment(Long userId,
                                  PaymentConfirmReqDto requestDto) {
        System.out.println("----------------------------------결제 트랜잭션 시작----------------------------------");

        // 1. 결제 엔티티 생성
        Payment payment = createPayment(userId, requestDto.orderId(), requestDto.tossOrderId(), requestDto.paymentKey(), requestDto.paymentType(), requestDto.amount(), PaymentStatus.PENDING);

        // 2. toss payments API 호출
        ResponseEntity<TossConfirmResDto> response = tossPaymentApi(new TossConfirmReqDto(requestDto.tossOrderId(),
                requestDto.paymentKey(),
                requestDto.amount()));


        paymentRepository.save(payment);

        // 3. 응답 검증
        // pending -> 자동 재시도, fail -> 수동 재시도, approve -> 완료
        switch (getStatusCodeToString(response)) {
            case "200" -> payment.successPayment();
            case "400" -> payment.failPayment("결제 실패");
            case "500" -> payment.pendingPayment();
        }

        System.out.println("----------------------------------결제 트랜잭션 종료----------------------------------");
        return payment;
    }

    /*
        결제 요청 생성
     */
    @Override
    public Payment createPayment(Long userId, Long orderId, String tossOrderId,
                                 String paymentKey, String paymentType, Long amount, PaymentStatus paymentStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserError.NOT_EXISTS_USER));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(PaymentError.ORDER_NOT_FOUND));

        Payment payment = Payment.create(user, order, tossOrderId, paymentKey, paymentType, amount, PaymentStatus.PENDING);
        paymentRepository.save(payment);
        return payment;
    }

    /*
        Toss 결제 요청 API 호출 (결제 승인)
     */
    @Override
    public ResponseEntity<TossConfirmResDto> tossPaymentApi(TossConfirmReqDto tossConfirmReqDto) {
        return paymentWebClientUtil.confirmPayment(tossConfirmReqDto, apiKey);
    }

    /*
        결제 취소
     */
    @Transactional
    public boolean cancelPayment(Long userId, PaymentCancelReqDto paymentCancelReqDto, PaymentStatus paymentStatus) {
        Payment payment = findPaymentByPaymentKey(paymentCancelReqDto.paymentKey());
        TossCancelReqDto tossCancelReqDto = new TossCancelReqDto(paymentCancelReqDto.cancelReason(), paymentCancelReqDto.cancelAmount());

        ResponseEntity<TossCancelResDto> response = tossPaymentCancelApi(tossCancelReqDto, paymentCancelReqDto.paymentKey());

        boolean res = validateResponse(response);
        if (res) {
            payment.cancelPayment();
        }

        return res;
    }

    /*
        Toss 결제 취소 API 호출
     */
    public ResponseEntity<TossCancelResDto> tossPaymentCancelApi(TossCancelReqDto tossCancelReqDto, String paymentKey) {
        return ResponseEntity.ok(paymentWebClientUtil.cancelPayment(tossCancelReqDto, apiKey, paymentKey));
    }

    @Transactional
    public Payment retryPayment(Long userId, String tossOrderId, String paymentKey) {
        Payment payment = paymentRepository.findTopLatestPaymentByOrderId(tossOrderId)
                .orElseThrow(() -> new CustomException(PaymentError.PAYMENT_NOT_FOUND));

        if (!payment.canRetry()) {
            throw new IllegalStateException("최대 결제 재시도 횟수를 초과했습니다.");
        }

        // 기존 결제 정보를 기반으로 새로운 결제 요청 생성
        PaymentConfirmReqDto retryRequest = new PaymentConfirmReqDto(
                payment.getOrder().getId(),
                payment.getTossOrderId(),
                payment.getPaymentKey(),
                payment.getAmount(),
                payment.getPaymentType(),
                0// 포인트는 이미 차감되었으므로 0으로 설정 -> 결제 서비스에서 포인트차감이 아님
        );
        return processPayment(userId, retryRequest);
    }

    /*
        HTTP 응답 검증
     */
    private <T> String getStatusCodeToString(ResponseEntity<T> response) {
        HttpStatusCode statusCode = response.getStatusCode();
        int statusValue = statusCode.value(); // 상태 코드 정수값 가져오기

        if (statusValue >= 200 && statusValue < 300) {
            return "200"; // 모든 2xx 응답을 200으로 변환
        } else if (statusValue >= 400 && statusValue < 500) {
            return "400"; // 모든 4xx 응답을 400으로 변환
        }

        return String.valueOf(statusValue); // 1xx, 3xx, 5xx 등은 원래 값 유지
    }

    public <T> boolean validateResponse(ResponseEntity<T> response) {
        return response.getStatusCode().is2xxSuccessful() && response.getBody() != null;
    }



    /*
        결제 취소
     */
    public boolean cancelPayment(Long userId, PaymentCancelReqDto paymentCancelReqDto) {

        //Payment 있는지 확인
        Payment payment = findPaymentByPaymentKey(paymentCancelReqDto.paymentKey());

        //Toss request 객체로 변환
        TossCancelReqDto tossCancelReqDto = new TossCancelReqDto(paymentCancelReqDto.cancelReason(), paymentCancelReqDto.cancelAmount());

        //tossApi 호출
        ResponseEntity<TossCancelResDto> response = tossPaymentCancelApi(tossCancelReqDto, paymentCancelReqDto.paymentKey());

        // response 정합성 검사
        boolean res = validateResponse(response);

        // body 추출
        TossCancelResDto tossResDto = response.getBody();

        // 취소 성공 로직 (payment 상태 canceled 로 변경)
        if (res) {
            payment.cancelPayment();
        }

        // 성공 여부 리턴
        return res;
    }

}