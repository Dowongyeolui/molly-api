package org.example.mollyapi.payment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.example.mollyapi.common.config.WebClientUtil;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.common.exception.error.impl.PaymentError;
import org.example.mollyapi.common.exception.error.impl.UserError;
import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.order.repository.OrderRepository;
import org.example.mollyapi.order.service.OrderService;
import org.example.mollyapi.payment.dto.request.PaymentCancelReqDto;
import org.example.mollyapi.payment.dto.request.TossCancelReqDto;
import org.example.mollyapi.payment.dto.response.PaymentResDto;
import org.example.mollyapi.payment.dto.response.TossCancelResDto;
import org.example.mollyapi.payment.dto.request.TossConfirmReqDto;
import org.example.mollyapi.payment.dto.response.TossConfirmResDto;
import org.example.mollyapi.payment.entity.Payment;
import org.example.mollyapi.payment.repository.PaymentRepository;
import org.example.mollyapi.payment.service.PaymentService;
import org.example.mollyapi.payment.util.PaymentWebClientUtil;
import org.example.mollyapi.user.dto.GetUserSummaryInfoWithPointResDto;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.example.mollyapi.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.Math.toIntExact;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final WebClientUtil webClientUtil;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final PaymentWebClientUtil paymentWebClientUtil;
    private final UserService userService;
    private final UserRepository userRepository;


    @Value("${secret.payment-api-key}")
    private String apiKey;

    /*
        결제 로직
     */
    @Transactional
    public Payment processPayment(Long userId, String paymentKey, String tossOrderId, Long amount, Integer point, Long deliveryId, String paymentType) {
        /* 1. find order with tossOrderId
         2. validate amount
         3. success/failure logic
         3-1 if failure -> throw exception
         4. create payment
         5. toss api
         6. success/failure logic
        */

        // user find
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserError.NOT_EXISTS_USER));

        // order findByTossOrderId
        Order order = orderRepository.findByTossOrderId(tossOrderId)
                .orElseThrow(()-> new CustomException(PaymentError.ORDER_NOT_FOUND));
        Long orderAmount = order.getTotalAmount();

        // 유저 포인트 검증
        validateUserPoint(userId, point);

        // 결제정보 검증
        validateAmount(orderAmount, amount);

        // payment API
        TossConfirmReqDto tossConfirmReqDto = new TossConfirmReqDto(paymentKey, tossOrderId, amount);
        ResponseEntity<TossConfirmResDto> response = tossPaymentApi(tossConfirmReqDto, apiKey);

        // response 정합성 검사
        boolean res = validateResponse(response);

        // api 응답 tossResDto로 추출
        TossConfirmResDto tossResDto = response.getBody();

        // create pending payment
        Payment payment = Payment.from(user, order, tossOrderId, paymentKey, paymentType, amount, "결제대기");

        // 결제 성공 및 실패 로직
        if (res) {
            successPayment(payment, tossOrderId, point);
        } else {
            failPayment(payment, tossOrderId, "실패");
        }
        paymentRepository.save(payment);
        return payment;
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
        ResponseEntity<TossCancelResDto> response = tossPaymentCancelApi(tossCancelReqDto,paymentCancelReqDto.paymentKey());

        // response 정합성 검사
        boolean res = validateResponse(response);

        // body 추출
        TossCancelResDto tossResDto = response.getBody();

        // 취소 성공 로직 (payment 상태 canceled 로 변경)
        if(res){ payment.cancelPayment(); }

        // 성공 여부 리턴
        return res;
    }

    @Override
    public Payment findPaymentByPaymentKey(String paymentKey) {
        return paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new CustomException(PaymentError.PAYMENT_NOT_FOUND));
    }

    /*
        결제 성공 - 주문 업데이트 (포인트, 상태), 포인트 차감
     */
    public void successPayment(Payment payment, String tossOrderId, Integer point) {
        //payment status change
        payment.successPayment(point);
        //order success (field update, point usage)
        orderService.successOrder(tossOrderId,payment.getPaymentKey(),payment.getPaymentType(),payment.getAmount(),point);

    }

    /*
        결제 실패 - 주문 업데이트
     */
    public void failPayment(Payment payment, String tossOrderId, String failureReason) {
        payment.failPayment(failureReason);
//        orderService.failOrder(tossOrderId);
        throw new CustomException(PaymentError.PAYMENT_FAILED);
    }


    /*
        confirm tossApi 호출
     */
    private ResponseEntity<TossConfirmResDto> tossPaymentApi(TossConfirmReqDto tossConfirmReqDto, String apiKey) {

        TossConfirmResDto tossConfirmResDto = paymentWebClientUtil.confirmPayment(tossConfirmReqDto,apiKey);
        return ResponseEntity.ok(tossConfirmResDto);
    }

    /*
        cancel tossApi 호출
     */
    private ResponseEntity<TossCancelResDto> tossPaymentCancelApi(TossCancelReqDto tossCancelReqDto, String paymentKey) {
        TossCancelResDto tossCancelResDto = paymentWebClientUtil.cancelPayment(tossCancelReqDto, apiKey, paymentKey);
        return ResponseEntity.ok(tossCancelResDto);
    }

    /*
        payment 생성
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    /*
        HTTP 응답 검증
     */
    private <T> boolean validateResponse(ResponseEntity<T> response) {
        return response.getStatusCode().is2xxSuccessful() && response.getBody() != null;
    }

    /*
        결제 금액 검증
     */
    private void validateAmount(Long orderAmount, Long amount) {
        if (!Objects.equals(amount,orderAmount)) {
            throw new CustomException(PaymentError.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    /*
        포인트 검증
     */
    private void validateUserPoint(Long userId, Integer requiredPoint) {
        GetUserSummaryInfoWithPointResDto userDto = userService.getUserSummaryWithPoint(userId);

        Integer availablePoint = userDto.point();

        if (requiredPoint > availablePoint) {
            throw new CustomException(PaymentError.PAYMENT_POINT_INSUFFICIENT);
        }
    }
}