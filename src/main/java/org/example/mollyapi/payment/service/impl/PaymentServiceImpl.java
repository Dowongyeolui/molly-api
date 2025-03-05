package org.example.mollyapi.payment.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.common.exception.error.impl.PaymentError;
import org.example.mollyapi.common.exception.error.impl.UserError;
import org.example.mollyapi.delivery.dto.DeliveryReqDto;
import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.order.repository.OrderRepository;
import org.example.mollyapi.payment.dto.request.PaymentCancelReqDto;
import org.example.mollyapi.payment.dto.request.PaymentRequestDto;
import org.example.mollyapi.payment.dto.request.TossCancelReqDto;
import org.example.mollyapi.payment.dto.request.TossConfirmReqDto;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


import static java.lang.Math.toIntExact;

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
    public PaymentInfoResDto findLatestPayment(Long orderId) {
        Pageable pageable = PageRequest.of(0, 1);
        return paymentRepository.findLatestPaymentByOrderId(orderId, pageable).stream()
                .findFirst()
                .map(PaymentInfoResDto::from)
                .orElseThrow(() -> new CustomException(PaymentError.PAYMENT_NOT_FOUND));
    }

    @Override
    public List<PaymentInfoResDto> findAllPayments(Long orderId) {
        return paymentRepository.findAllByOrderByCreatedAtDesc()
                .orElseThrow(() -> new CustomException(PaymentError.PAYMENT_NOT_FOUND))
                .stream()
                .map(PaymentInfoResDto::from)
                .collect(Collectors.toList());
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
    @Transactional
    public Payment processPayment(User user, Order order,
                                  PaymentRequestDto requestDto) {

        // 1. 결제 엔티티 생성
        Payment payment = Payment.create(
                user,
                requestDto.tossOrderId(),
                requestDto.paymentKey(),
                requestDto.paymentType(),
                requestDto.amount(),
                PaymentStatus.PENDING
        );

        // 2. toss payments API 호출
        ResponseEntity<TossConfirmResDto> response = tossPaymentApi(new TossConfirmReqDto(requestDto.tossOrderId(),
                requestDto.paymentKey(),
                requestDto.amount()));

        // 3. 응답 검증
        boolean isSuccess = validateResponse(response);

        // 결제 실패 시 예외 처리
        if (!isSuccess) {
            payment.failPayment("결제 실패");
            paymentRepository.save(payment);
//            throw new CustomException(PaymentError.PAYMENT_FAILED);
            return payment; // 결제 재시도 가능하도록
        }
        payment.successPayment(order);
        paymentRepository.save(payment);
        return payment;
    }

    /*
        결제 요청 생성
     */
    @Transactional
    public Payment createPayment(Long userId, String tossOrderId,
                                 String paymentKey, String paymentType, Long amount, PaymentStatus paymentStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserError.NOT_EXISTS_USER));

        Payment payment = Payment.create(user, tossOrderId, paymentKey, paymentType, amount, PaymentStatus.PENDING);
        paymentRepository.save(payment);
        return payment;
    }

    /*
        Toss 결제 요청 API 호출 (결제 승인)
     */
    @Override
    public ResponseEntity<TossConfirmResDto> tossPaymentApi(TossConfirmReqDto tossConfirmReqDto) {
        return ResponseEntity.ok(paymentWebClientUtil.confirmPayment(tossConfirmReqDto, apiKey));
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
        Payment payment = paymentRepository.findByTossOrderIdAndUserId(tossOrderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다. tossOrderId=" + tossOrderId));

        if (!payment.canRetry()) {
            throw new IllegalStateException("최대 결제 재시도 횟수를 초과했습니다.");
        }

        // 기존 결제 정보를 기반으로 새로운 결제 요청 생성
        PaymentRequestDto retryRequest = new PaymentRequestDto(
                payment.getTossOrderId(),
                payment.getPaymentKey(),
                payment.getAmount(),
                payment.getPaymentType(),
                "0", // 포인트는 이미 차감되었으므로 0으로 설정
                DeliveryReqDto.from(payment.getOrder().getDelivery()) // 기존 배송 정보 사용
        );
        return processPayment(userId, retryRequest);
    }

    /*
        HTTP 응답 검증
     */
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