package org.example.mollyapi.payment;


import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.common.exception.error.impl.PaymentError;
import org.example.mollyapi.delivery.dto.DeliveryReqDto;
import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.order.entity.OrderDetail;
import org.example.mollyapi.order.repository.OrderRepository;
import org.example.mollyapi.order.service.OrderService;
import org.example.mollyapi.order.type.CancelStatus;
import org.example.mollyapi.order.type.OrderStatus;
import org.example.mollyapi.payment.dto.request.PaymentConfirmReqDto;
import org.example.mollyapi.payment.dto.request.PaymentRequestDto;
import org.example.mollyapi.payment.dto.response.TossConfirmResDto;
import org.example.mollyapi.payment.entity.Payment;
import org.example.mollyapi.payment.repository.PaymentRepository;
import org.example.mollyapi.payment.service.PaymentService;
import org.example.mollyapi.payment.service.impl.PaymentServiceImpl;
import org.example.mollyapi.payment.type.PaymentStatus;
import org.example.mollyapi.payment.util.PaymentWebClientUtil;
import org.example.mollyapi.product.entity.Category;
import org.example.mollyapi.product.entity.Product;
import org.example.mollyapi.product.repository.CategoryRepository;
import org.example.mollyapi.product.repository.ProductRepository;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.example.mollyapi.user.type.Sex;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.BeforeTransaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
public class PaymentServiceTest {

    private PaymentServiceImpl paymentServiceImpl;

    @Mock
    private PaymentWebClientUtil paymentWebClientUtil;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private User user;
    private Order order;
    Long orderAmount = 10000L;
    String tossOrderId = "ORD-20250213132349-6572";
    String paymentKey = "PAY-20250213132349-6572";
    LocalDate userRegisterDate = LocalDate.now().minusDays(1);
    LocalDateTime orderDate = LocalDateTime.now().minusDays(1);

    @BeforeEach
    void setUp() {

        // Mock Stubbing
        user = new User(1L, "mo","01051345633", Sex.MALE,true,"www.example.com", userRegisterDate,5000,"Jerry");
        userRepository.save(user);

        // Mock paymentWebClientUtil
        TossConfirmResDto mockResponse = new TossConfirmResDto(
                null,  // mId
                null,  // version
                "tORD-20250213132349-6572",  // paymentKey
                "SUCCESS",  // status
                null,  // lastTransactionKey
                null,  // method
                tossOrderId,  // orderId
                null,  // orderName
                10000L,  // totalAmount
                null,  // card (nullable)
                null   // easyPay (nullable)
        );
    }

    @DisplayName("Toss API 에서 2xx 응답을 리턴합니다. 결제는 APPROVED 상태로 저장됩니다..")
    @Test
    void processPaymentWithToss2xxResponse(){
        //given
        LocalDateTime orderDate = LocalDateTime.now();
        order = new Order(1L,tossOrderId,user,null,null,null, 10000L, 0L,paymentKey,"NORMAL",0,0, OrderStatus.PENDING, CancelStatus.NONE, orderDate, orderDate.plusMinutes(30));
        orderRepository.save(order);

        PaymentConfirmReqDto paymentConfirmReqDto = new PaymentConfirmReqDto(order.getId(), order.getTossOrderId(), order.getPaymentId(), order.getTotalAmount(),order.getPaymentType(),order.getPointUsage());

        ResponseEntity<TossConfirmResDto> response = ResponseEntity
                .status(HttpStatus.CREATED)
                .body(null);
        given(paymentWebClientUtil.confirmPayment(any(), any())).willReturn(response);
        paymentServiceImpl = new PaymentServiceImpl(paymentRepository, paymentWebClientUtil,userRepository, orderRepository);

        //when
        Payment payment = paymentServiceImpl.processPayment(
                user.getUserId(),
                paymentConfirmReqDto
        );


        //then
        assertThat(payment)
                .extracting(Payment::getPaymentStatus, Payment::getTossOrderId, Payment::getAmount, Payment::getPaymentType)
                .isEqualTo(List.of(PaymentStatus.APPROVED, tossOrderId, order.getTotalAmount(), "NORMAL"));

    }


    @DisplayName("Toss API 에서 4xx 에러를 리턴합니다. 결제는 FAILED 상태로 존재합니다..")
    @Test
    void processPaymentWithToss4xxError() {

        //given
        LocalDateTime orderDate = LocalDateTime.now();
        order = new Order(1L,tossOrderId,user,null,null,null, 10000L, 0L,paymentKey,"NORMAL",0,0, OrderStatus.PENDING, CancelStatus.NONE, orderDate, orderDate.plusMinutes(30));
        orderRepository.save(order);

        PaymentConfirmReqDto paymentConfirmReqDto = new PaymentConfirmReqDto(order.getId(), order.getTossOrderId(), order.getPaymentId(), order.getTotalAmount(),order.getPaymentType(),order.getPointUsage());

        ResponseEntity<TossConfirmResDto> response = ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(null);
        given(paymentWebClientUtil.confirmPayment(any(), any())).willReturn(response);
        paymentServiceImpl = new PaymentServiceImpl(paymentRepository, paymentWebClientUtil,userRepository, orderRepository);

        //when
        Payment payment = paymentServiceImpl.processPayment(
                user.getUserId(),
                paymentConfirmReqDto
        );


        //then
        assertThat(payment)
                .extracting(Payment::getPaymentStatus, Payment::getTossOrderId, Payment::getAmount, Payment::getPaymentType)
                .isEqualTo(List.of(PaymentStatus.FAILED, tossOrderId, order.getTotalAmount(), "NORMAL"));

    }

    @DisplayName("Toss API 에서 5xx 에러를 리턴합니다. 결제는 PENDING 상태로 생성됩니다.")
    @Test
    void processPaymentWithToss5xxError(){
        //given
        LocalDateTime orderDate = LocalDateTime.now();
        order = new Order(1L,tossOrderId,user,null,null,null, 10000L, 0L,paymentKey,"NORMAL",0,0, OrderStatus.PENDING, CancelStatus.NONE, orderDate, orderDate.plusMinutes(30));
        orderRepository.save(order);

        PaymentConfirmReqDto paymentConfirmReqDto = new PaymentConfirmReqDto(order.getId(), order.getTossOrderId(), order.getPaymentId(), order.getTotalAmount(),order.getPaymentType(),order.getPointUsage());

        ResponseEntity<TossConfirmResDto> response = ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(null);
        given(paymentWebClientUtil.confirmPayment(any(), any())).willReturn(response);
        paymentServiceImpl = new PaymentServiceImpl(paymentRepository, paymentWebClientUtil,userRepository, orderRepository);

        //when
        Payment payment = paymentServiceImpl.processPayment(
                user.getUserId(),
                paymentConfirmReqDto
        );


        //then
        assertThat(payment)
                .extracting(Payment::getPaymentStatus, Payment::getTossOrderId, Payment::getAmount, Payment::getPaymentType)
                .isEqualTo(List.of(PaymentStatus.PENDING, tossOrderId, order.getTotalAmount(), "NORMAL"));

    }

//    @DisplayName("이미 완료된 주문에 대해 결제를 시도합니다")
//    @Test
//    void processPaymentFromFinishedOrder(){
//
//        //given
//        LocalDateTime orderDate = LocalDateTime.now();
//        order = new Order(1L,tossOrderId,user,null,null,null, 10000L, 0L,paymentKey,"NORMAL",0,0, OrderStatus.SUCCEEDED, CancelStatus.NONE, orderDate, orderDate.plusMinutes(30));
//        given(orderRepository.findByTossOrderId(any())).willReturn(Optional.of(order));
//
//        LocalDateTime paymentDate = LocalDateTime.now().plusMinutes(10);
//        Payment invalidPayment = new Payment(1L,"NORMAL",orderAmount,"tORD-20250213132349-6572",tossOrderId,paymentDate,null,0,null,order,user);
//        given(paymentRepository.save(any())).willReturn(invalidPayment);
//
//        PaymentConfirmReqDto paymentConfirmReqDto = new PaymentConfirmReqDto(order.getId(), order.getTossOrderId(), order.getPaymentId(), order.getTotalAmount(), order.getPaymentType());
//
//        //when
//        CustomException exception = assertThrows(
//                CustomException.class,
//                () -> paymentServiceImpl.processPayment(
//                        user.getUserId(),
//                        paymentConfirmReqDto.paymentKey(),
//                        paymentConfirmReqDto.tossOrderId(),
//                        paymentConfirmReqDto.amount(),
//                        paymentConfirmReqDto.paymentType()
//                )
//        );
//
//        //then
//        assertThat(exception.getMessage()).isEqualTo(PaymentError.PAYMENT_ALREADY_PROCESSED.getMessage());
//        assertThat(exception.getHttpStatus()).isEqualTo(PaymentError.PAYMENT_ALREADY_PROCESSED.getStatus());
//    }
//
//    @DisplayName("만료된 주문에 대해 결제를 시도합니다.")
//    @Test
//    void processPaymentFromExpiredOrder(){
//
//        //given
//        LocalDateTime orderDate = LocalDateTime.now().minusMinutes(30);
//        LocalDateTime expiredOrderDate = orderDate.plusMinutes(10);
//        order = new Order(1L,tossOrderId,user,null,null,null, 10000L, 0L,paymentKey,"NORMAL",0,0, OrderStatus.PENDING, CancelStatus.NONE, orderDate, expiredOrderDate);
//        given(orderRepository.findByTossOrderId(any())).willReturn(Optional.of(order));
//
//        LocalDateTime paymentDate = LocalDateTime.now();
//        Payment invalidPayment = new Payment(1L,"NORMAL",orderAmount,"tORD-20250213132349-6572",tossOrderId,paymentDate,null,0,null,order,user);
//        given(paymentRepository.save(any())).willReturn(invalidPayment);
//
//        PaymentConfirmReqDto paymentConfirmReqDto = new PaymentConfirmReqDto(order.getId(), order.getTossOrderId(), order.getPaymentId(), order.getTotalAmount(), order.getPaymentType());
//
//        //when
//        CustomException exception = assertThrows(
//                CustomException.class,
//                () -> paymentServiceImpl.processPayment(
//                        user.getUserId(),
//                        paymentConfirmReqDto.paymentKey(),
//                        paymentConfirmReqDto.tossOrderId(),
//                        paymentConfirmReqDto.amount(),
//                        paymentConfirmReqDto.paymentType()
//                )
//        );
//
//        //then
//        assertThat(exception.getMessage()).isEqualTo(PaymentError.ORDER_EXPIRED.getMessage());
//        assertThat(exception.getHttpStatus()).isEqualTo(PaymentError.ORDER_EXPIRED.getStatus());
//
//    }
//
//    @DisplayName("결제 요청 금액과 주문정보의 금액이 일치하지 않습니다.")
//    @Test
//    void paymentAmountMismatch(){
//        //given
//        LocalDateTime orderDate = LocalDateTime.now();
//        LocalDateTime expiredOrderDate = orderDate.plusMinutes(10);
//        order = new Order(1L,tossOrderId,user,null,null,null, 10000L, 0L,paymentKey,"NORMAL",0,0, OrderStatus.PENDING, CancelStatus.NONE, orderDate, expiredOrderDate);
//        given(orderRepository.findByTossOrderId(any())).willReturn(Optional.of(order));
//
////        LocalDateTime paymentDate = LocalDateTime.now();
//        Long wrongAmount = orderAmount+5000;
////        Payment invalidPayment = new Payment(1L,"NORMAL",wrongAmount,"tORD-20250213132349-6572",tossOrderId,paymentDate,null,0,null,order,user);
////        given(paymentRepository.save(any())).willReturn(invalidPayment);
//
//        PaymentConfirmReqDto paymentConfirmReqDto = new PaymentConfirmReqDto(order.getId(), order.getTossOrderId(), order.getPaymentId(), wrongAmount, order.getPaymentType());
//
//        //when
//        CustomException exception = assertThrows(
//                CustomException.class,
//                () -> paymentServiceImpl.processPayment(
//                        user.getUserId(),
//                        paymentConfirmReqDto.paymentKey(),
//                        paymentConfirmReqDto.tossOrderId(),
//                        paymentConfirmReqDto.amount(),
//                        paymentConfirmReqDto.paymentType()
//                )
//       );
//
//        //then
//        assertThat(exception.getMessage()).isEqualTo(PaymentError.PAYMENT_AMOUNT_MISMATCH.getMessage());
//        assertThat(exception.getHttpStatus()).isEqualTo(PaymentError.PAYMENT_AMOUNT_MISMATCH.getStatus());
//    }
}
