package org.example.mollyapi.payment;


import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.common.exception.error.impl.PaymentError;
import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.order.repository.OrderRepository;
import org.example.mollyapi.order.type.CancelStatus;
import org.example.mollyapi.order.type.OrderStatus;
import org.example.mollyapi.payment.dto.request.PaymentConfirmReqDto;
import org.example.mollyapi.payment.dto.response.TossConfirmResDto;
import org.example.mollyapi.payment.entity.Payment;
import org.example.mollyapi.payment.repository.PaymentRepository;
import org.example.mollyapi.payment.service.impl.PaymentServiceImpl;
import org.example.mollyapi.payment.util.PaymentWebClientUtil;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.example.mollyapi.user.type.Sex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class PaymentWebClientTest {

    @Autowired
    private PaymentWebClientUtil paymentWebClientUtil;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentRepository paymentRepository;

    private PaymentServiceImpl paymentServiceImpl;

    Long orderAmount = 10000L;
    String tossOrderId = "ORD-20250213132349-6572";
    String paymentKey = "PAY-20250213132349-6572";
    LocalDate userRegisterDate = LocalDate.now().minusDays(1);

    private User user;
    private Order order;

    @BeforeEach
    void setUp() {

        user = new User(1L, "mo","01051345633", Sex.MALE,true,"www.example.com", userRegisterDate,5000,"Jerry");
        given(userRepository.findById(any())).willReturn(Optional.of(user));

        LocalDateTime orderDate = LocalDateTime.now();
        LocalDateTime expiredOrderDate = orderDate.plusMinutes(10);
        order = new Order(1L,tossOrderId,user,null,null,null, orderAmount, 0L,paymentKey,"NORMAL",0,0, OrderStatus.PENDING, CancelStatus.NONE, orderDate, expiredOrderDate);
        given(orderRepository.findByTossOrderId(any())).willReturn(Optional.of(order));

        paymentServiceImpl = new PaymentServiceImpl(paymentRepository,orderRepository, paymentWebClientUtil,userRepository);

    }

    @DisplayName("paymentKey, orderId가 toss api 에서 조회되지 않는 경우")
    @Test
    void notFoundPayment(){
        //given
        ReflectionTestUtils.setField(paymentServiceImpl, "apiKey", "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6");
        PaymentConfirmReqDto paymentConfirmReqDto = new PaymentConfirmReqDto(order.getId(), order.getTossOrderId(), order.getPaymentId(), order.getTotalAmount(), order.getPaymentType());
        //when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> paymentServiceImpl.processPayment(
                        user.getUserId(),
                        paymentConfirmReqDto.paymentKey(),
                        paymentConfirmReqDto.tossOrderId(),
                        paymentConfirmReqDto.amount(),
                        paymentConfirmReqDto.paymentType()
                )
        );

        //then
        assertThat(exception.getMessage()).isEqualTo("결제 시간이 만료되어 결제 진행 데이터가 존재하지 않습니다.");


    }

    @DisplayName("api key가 등록되지 않았거나, 잘못되었을 경우")
    @Test
    void invalidApiKey(){
        //given
        ReflectionTestUtils.setField(paymentServiceImpl, "apiKey", "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw2");
        PaymentConfirmReqDto paymentConfirmReqDto = new PaymentConfirmReqDto(order.getId(), order.getTossOrderId(), order.getPaymentId(), order.getTotalAmount(), order.getPaymentType());
        //when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> paymentServiceImpl.processPayment(
                        user.getUserId(),
                        paymentConfirmReqDto.paymentKey(),
                        paymentConfirmReqDto.tossOrderId(),
                        paymentConfirmReqDto.amount(),
                        paymentConfirmReqDto.paymentType()
                )
        );

        //then
        assertThat(exception.getMessage()).isEqualTo("인증되지 않은 시크릿 키 혹은 클라이언트 키 입니다.");

    }

    @DisplayName("toss api에서 이미 처리된 결제일 경우")
    @Test
    void alreadyProcessedPayment(){
        //given
        ReflectionTestUtils.setField(paymentServiceImpl, "apiKey", "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6");
        PaymentConfirmReqDto paymentConfirmReqDto = new PaymentConfirmReqDto(order.getId(), order.getTossOrderId(), order.getPaymentId(), order.getTotalAmount(), order.getPaymentType());
        //when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> paymentServiceImpl.processPayment(
                        user.getUserId(),
                        paymentConfirmReqDto.paymentKey(),
                        paymentConfirmReqDto.tossOrderId(),
                        paymentConfirmReqDto.amount(),
                        paymentConfirmReqDto.paymentType()
                )
        );

        //then
        assertThat(exception.getMessage()).isEqualTo("존재하지 않는 결제 정보 입니다.");
    }


    @DisplayName("toss api 응답 오류")
    @Test
    void unknownPaymentError(){
        //given (need mock stubbing)
        ReflectionTestUtils.setField(paymentServiceImpl, "apiKey", "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6");
        PaymentConfirmReqDto paymentConfirmReqDto = new PaymentConfirmReqDto(order.getId(), order.getTossOrderId(), order.getPaymentId(), order.getTotalAmount(), order.getPaymentType());
        //when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> paymentServiceImpl.processPayment(
                        user.getUserId(),
                        paymentConfirmReqDto.paymentKey(),
                        paymentConfirmReqDto.tossOrderId(),
                        paymentConfirmReqDto.amount(),
                        paymentConfirmReqDto.paymentType()
                )
        );

        //then
        assertThat(exception.getMessage()).isEqualTo("존재하지 않는 결제 정보 입니다.");
    }





}
