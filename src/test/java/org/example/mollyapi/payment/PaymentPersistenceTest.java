//package org.example.mollyapi.payment;
//
//import lombok.extern.slf4j.Slf4j;
//import org.example.mollyapi.order.entity.Order;
//import org.example.mollyapi.order.repository.OrderRepository;
//import org.example.mollyapi.order.type.CancelStatus;
//import org.example.mollyapi.order.type.OrderStatus;
//import org.example.mollyapi.payment.dto.request.PaymentConfirmReqDto;
//import org.example.mollyapi.payment.dto.response.TossConfirmResDto;
//import org.example.mollyapi.payment.entity.Payment;
//import org.example.mollyapi.payment.repository.PaymentRepository;
//import org.example.mollyapi.payment.service.impl.PaymentServiceImpl;
//import org.example.mollyapi.payment.type.PaymentStatus;
//import org.example.mollyapi.payment.util.PaymentWebClientUtil;
//import org.example.mollyapi.user.entity.User;
//import org.example.mollyapi.user.repository.UserRepository;
//import org.example.mollyapi.user.type.Sex;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.given;
//
//@Slf4j
//@SpringBootTest
//@ActiveProfiles("test")
//public class PaymentPersistenceTest {
//
//    @Mock
//    private PaymentWebClientUtil paymentWebClientUtil;
//
//    @Autowired
//    private OrderRepository orderRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private PaymentRepository paymentRepository;
//
//    private PaymentServiceImpl paymentServiceImpl;
//
//
//
//    @BeforeEach
//    public void setUp() {
//
//        Long orderAmount = 10000L;
//        String tossOrderId = "ORD-20250213132349-6572";
//        String paymentKey = "PAY-20250213132349-6572";
//        LocalDate userRegisterDate = LocalDate.now().minusDays(1);
//        LocalDateTime orderDate = LocalDateTime.now();
//        LocalDateTime expiredOrderDate = orderDate.plusMinutes(10);
//
//        User user = new User(1L, "mo","01051345633", Sex.MALE,true,"www.example.com", userRegisterDate,5000,"Jerry");
//        userRepository.save(user);
//
//        Order order = new Order(1L,tossOrderId,user,null,null,null, orderAmount, 0L,paymentKey,"NORMAL",0,0, OrderStatus.PENDING, CancelStatus.NONE, orderDate, expiredOrderDate);
//        orderRepository.save(order);
//
//        TossConfirmResDto mockResponse = new TossConfirmResDto(
//                null,  // mId
//                null,  // version
//                paymentKey,  // paymentKey
//                "SUCCESS",  // status
//                null,  // lastTransactionKey
//                null,  // method
//                tossOrderId,  // orderId
//                null,  // orderName
//                10000L,  // totalAmount
//                null,  // card (nullable)
//                null   // easyPay (nullable)
//        );
//
//        given(paymentWebClientUtil.confirmPayment(any(), any())).willReturn(mockResponse);
//        paymentServiceImpl = new PaymentServiceImpl(paymentRepository,orderRepository, paymentWebClientUtil,userRepository);
//    }
//
//    @Test
//    void savePayment() {
//        //given
//        User user = userRepository.findById(1L)
//                .orElseThrow(RuntimeException::new);
//        assertThat(user.getUserId()).isEqualTo(1L);
//
//        Order order = orderRepository.findById(1L)
//                .orElseThrow(RuntimeException::new);
//        assertThat(order.getId()).isEqualTo(1L);
//
//
//        Long countBefore = paymentRepository.count();
//
//        PaymentConfirmReqDto paymentConfirmReqDto = new PaymentConfirmReqDto(order.getId(), order.getTossOrderId(), order.getPaymentId(), order.getTotalAmount(), order.getPaymentType());
//
//        //when
//        Payment payment = paymentServiceImpl.processPayment(
//                user.getUserId(),
//                paymentConfirmReqDto.paymentKey(),
//                paymentConfirmReqDto.tossOrderId(),
//                paymentConfirmReqDto.amount(),
//                paymentConfirmReqDto.paymentType()
//        );
//
//        //then
//        assertThat(payment).isNotNull();
//
//        Payment savedPayment = paymentRepository.findById(payment.getId())
//                .orElseThrow(() -> new RuntimeException("Payment not found"));
//
//        assertThat(savedPayment)
//                .extracting(Payment::getPaymentStatus, Payment::getTossOrderId, Payment::getAmount, Payment::getPaymentType)
//                .isEqualTo(List.of(PaymentStatus.APPROVED, order.getTossOrderId(), order.getTotalAmount(), "NORMAL"));
//
//        assertThat(countBefore+1).isEqualTo(paymentRepository.count());
//    }
//
//
//
//
//}
