package org.example.mollyapi.payment.service;


import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.common.exception.error.impl.PaymentError;
import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.order.repository.OrderRepository;
import org.example.mollyapi.order.service.OrderService;
import org.example.mollyapi.order.type.CancelStatus;
import org.example.mollyapi.order.type.OrderStatus;
import org.example.mollyapi.payment.dto.request.PaymentConfirmReqDto;
import org.example.mollyapi.payment.dto.response.PaymentInfoResDto;
import org.example.mollyapi.payment.dto.response.TossConfirmResDto;
import org.example.mollyapi.payment.entity.Payment;
import org.example.mollyapi.payment.exception.RetryablePaymentException;
import org.example.mollyapi.payment.repository.PaymentRepository;
import org.example.mollyapi.payment.service.impl.PaymentServiceImpl;
import org.example.mollyapi.payment.type.PaymentStatus;
import org.example.mollyapi.payment.util.PaymentWebClientUtil;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.example.mollyapi.user.type.Sex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Slf4j
@Transactional
@ActiveProfiles("test")
public class PaymentServiceImplTest {


    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentService paymentService;

    @MockBean
    private PaymentWebClientUtil paymentWebClientUtil;

    User user;
    Order order;
    Payment payment1;
    Payment payment2;
    Payment payment3;


    @Autowired
    private PaymentServiceImpl paymentServiceImpl;


    @BeforeEach
    void setUp() {
        //given
        user = createUser("momo");
        order = createOrder(user, "ord-20250213132349-6572", 50000L);
        payment1 = createPayment(user,order,"pay-20250213132349-6572",50000L);
        payment2 = createPayment(user,order,"pay-20250213132349-6573",50000L);
        payment3 = createPayment(user,order,"pay-20250213132349-6574",50000L);

        userRepository.save(user);
        orderRepository.save(order);
        paymentRepository.saveAll(List.of(payment1, payment2, payment3));
    }


    @Test
    void findLatestPayments(){

        //given
        Long orderId = order.getId();

        //when
        PaymentInfoResDto paymentInfoResDto = paymentService.findLatestPayment(orderId)
                .orElseThrow();

        //then
        assertThat(paymentInfoResDto)
                .extracting("paymentId","amount")
                .contains(payment3.getId(),payment3.getAmount());
    }

    @Test
    void findUserPayments(){

        //given
        Long userId = user.getUserId();

        //when
        List<PaymentInfoResDto> paymentInfoResDtos = paymentService.findUserPayments(userId);

        //then
        assertThat(paymentInfoResDtos)
                .extracting("paymentId","amount")
                .hasSize(3)
                .containsExactlyInAnyOrder(
                        tuple(payment1.getId(), 50000L),
                        tuple(payment2.getId(), 50000L),
                        tuple(payment3.getId(), 50000L)
                );
    }

    @DisplayName("재시도후 성공 (2xx)")
    @Test
    void retryPaymentWithSuccess(){

        //given
        Long userId = user.getUserId();
        String tossOrderId = order.getTossOrderId();
        String paymentKey = payment1.getPaymentKey();

        ResponseEntity<TossConfirmResDto> pendingResponse = getResponse(HttpStatus.BAD_GATEWAY);
        ResponseEntity<TossConfirmResDto> successResponse = getResponse(HttpStatus.OK);
        given(paymentWebClientUtil.confirmPayment(any(), any()))
                .willReturn(
                        pendingResponse,
                        pendingResponse,
                        successResponse
                );

        PaymentConfirmReqDto paymentConfirmReqDto = new PaymentConfirmReqDto(order.getId(), order.getTossOrderId(), order.getPaymentId(), order.getTotalAmount(),order.getPaymentType(),order.getPointUsage());

        //when
        long start = System.currentTimeMillis();
        Payment newPayment = paymentServiceImpl.processPayment(userId,paymentConfirmReqDto);
        long elapsed = System.currentTimeMillis() - start;

        //then
        assertThat(newPayment)
                .extracting("paymentStatus")
                .isEqualTo(PaymentStatus.APPROVED);
        assertThat(elapsed).isGreaterThanOrEqualTo(1000L);
    }

    @DisplayName("2번의 재시도 후 실패 (5xx)")
    @Test
    void retryPaymentWithPending(){

        //given
        Long userId = user.getUserId();
        String tossOrderId = order.getTossOrderId();
        String paymentKey = payment1.getPaymentKey();

        ResponseEntity<TossConfirmResDto> response = getResponse(HttpStatus.BAD_GATEWAY);
        given(paymentWebClientUtil.confirmPayment(any(), any())).willReturn(response);

        PaymentConfirmReqDto paymentConfirmReqDto = new PaymentConfirmReqDto(order.getId(), order.getTossOrderId(), order.getPaymentId(), order.getTotalAmount(),order.getPaymentType(),order.getPointUsage());

        // when
        long start = System.currentTimeMillis();

        assertThatThrownBy(() -> paymentServiceImpl
                .processPayment(userId, paymentConfirmReqDto))
                .isInstanceOf(RetryablePaymentException.class);

        long elapsed = System.currentTimeMillis() - start;

        // then
        assertThat(elapsed)
                .as("경과시간은 최소 backoff delay의 합보다 커야합니다. (>=3000ms) ")
                .isGreaterThanOrEqualTo(3000L);
    }

    @DisplayName("새로운 결제를 생성합니다")
    @Test
    void createPayment(){

        //given
        Long userId = user.getUserId();
        Long orderId = order.getId();
        String paymentKey = "pay-20250213132349-6575";
        String tossOrderId = "ord-20250213132349-6572";
        String paymentType = "NORMAL";
        Long amount = 50000L;
        PaymentStatus paymentStatus = PaymentStatus.PENDING;

        //when
        Payment newPayment = paymentService.createOrGetPayment(userId,orderId,tossOrderId,paymentKey,paymentType,amount);

        //then
        assertThat(newPayment)
                .extracting("tossOrderId", "paymentKey","paymentStatus")
                .contains(tossOrderId,paymentKey,paymentStatus);
    }

    @DisplayName("중복된 PaymentKey에 결제 생성 요청이 들어오면, 기존 결제를 반환합니다.")
    @Test
    void createOrGetPayment(){

        //given
        Long userId = user.getUserId();
        Long orderId = order.getId();

        String paymentKey = payment1.getPaymentKey();
        String tossOrderId = payment1.getTossOrderId();
        String paymentType = payment1.getPaymentType();
        Long amount = 50000L;
        PaymentStatus paymentStatus = payment1.getPaymentStatus();

        //when
        Payment newPayment = paymentService.createOrGetPayment(userId,orderId,tossOrderId,paymentKey,paymentType,amount);

        //then
        assertThat(newPayment)
                .extracting("tossOrderId", "paymentKey","paymentStatus")
                .contains(tossOrderId,paymentKey,paymentStatus);
    }





    private User createUser(String nickname) {
        return User.builder()
                .sex(Sex.MALE)
                .nickname(nickname)
                .cellPhone("01051212121")
                .birth(LocalDate.of(1990, 1, 1))
                .profileImage("ss")
                .flag(true)
                .build();
    }

    private Order createOrder(User user, String tossOrderId, Long amount) {
        return Order.builder()
                .tossOrderId(tossOrderId)
                .orderedAt(LocalDateTime.now())
                .totalAmount(amount)
                .user(user)
                .cancelStatus(CancelStatus.NONE)
                .expirationTime(LocalDateTime.now().plusDays(1))
                .status(OrderStatus.PENDING)
                .build();
    }

    private Payment createPayment(User user, Order order, String paymentKey, Long amount) {
        return Payment.create(
                user,
                order,
                order.getTossOrderId(),
                paymentKey,
                "NORMAL",
                amount
        );
    }



    private ResponseEntity<TossConfirmResDto> getResponse(HttpStatus status) {
        return ResponseEntity
                .status(status)
                .body(null);
    }

}
