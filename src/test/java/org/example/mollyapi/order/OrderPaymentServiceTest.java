package org.example.mollyapi.order;


import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.address.repository.AddressRepository;
import org.example.mollyapi.cart.repository.CartRepository;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.common.exception.error.impl.OrderError;
import org.example.mollyapi.common.exception.error.impl.PaymentError;
import org.example.mollyapi.common.exception.error.impl.ProductItemError;
import org.example.mollyapi.delivery.dto.DeliveryReqDto;
import org.example.mollyapi.delivery.repository.DeliveryRepository;
import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.order.entity.OrderDetail;
import org.example.mollyapi.order.repository.OrderDetailRepository;
import org.example.mollyapi.order.repository.OrderRepository;
import org.example.mollyapi.order.service.OrderServiceImpl;
import org.example.mollyapi.order.type.CancelStatus;
import org.example.mollyapi.order.type.OrderStatus;
import org.example.mollyapi.payment.dto.request.PaymentConfirmReqDto;
import org.example.mollyapi.payment.dto.request.PaymentRequestDto;
import org.example.mollyapi.payment.dto.response.PaymentInfoResDto;
import org.example.mollyapi.payment.dto.response.PaymentResDto;
import org.example.mollyapi.payment.entity.Payment;
import org.example.mollyapi.payment.repository.PaymentRepository;
import org.example.mollyapi.payment.service.PaymentService;
import org.example.mollyapi.payment.type.PaymentStatus;
import org.example.mollyapi.payment.util.AESUtil;
import org.example.mollyapi.payment.util.PaymentWebClientUtil;
import org.example.mollyapi.product.entity.Category;
import org.example.mollyapi.product.entity.Product;
import org.example.mollyapi.product.entity.ProductItem;
import org.example.mollyapi.product.repository.ProductItemRepository;
import org.example.mollyapi.product.repository.ProductRepository;
import org.example.mollyapi.review.repository.ReviewRepository;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.example.mollyapi.user.type.Sex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@Slf4j
@SpringBootTest
@ActiveProfiles("test2")
//@Transactional
//@Commit
@Sql(scripts = "/setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class OrderPaymentServiceTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    OrderRepository orderRepository;

    @Mock
    DeliveryRepository deliveryRepository;

    @Mock
    PaymentService paymentService;

    @Autowired
    ProductItemRepository productItemRepository;

    @Autowired
    OrderDetailRepository orderDetailRepository;

    @Mock
    CartRepository cartRepository;


    @Mock
    PaymentRepository paymentRepository;
    @Mock
    AddressRepository addressRepository;
    @Mock
    ReviewRepository reviewRepository;

    @MockBean
    AESUtil aesUtil;


    private User user;
    private Order order;
    Long orderAmount = 50000L;
    String tossOrderId = "ORD-20250213132349-6572";
    String paymentKey = "PAY-20250213132349-6572";
    LocalDate userRegisterDate = LocalDate.now().minusDays(1);

    String point = "0";

//    @Autowired
    private OrderServiceImpl orderServiceImpl;

    @Autowired
    private ProductRepository productRepository;


    @DisplayName("토스페이먼츠에서 4xx 응답을 보내주었을 때 예외를 리턴하고, 재고는 감소됩니다")
    @Test
    public void testOrderFor4xxResponse() {
        //given
        //user, product, product_item (sql)

        //결제 실패 (toss 4xx response)
        PaymentStatus paymentStatus = PaymentStatus.FAILED;

        // AESUtil Mocking
        MockedStatic<AESUtil> mockedStatic = mockStatic(AESUtil.class);
        mockedStatic.when(() -> aesUtil.decryptWithSalt(anyString()))
                .thenReturn("0");

        // 배송 mocking
        given(deliveryRepository.save(any())).willReturn(null);

        // paymentService findLatestPayment mocking
        given(paymentService.findLatestPayment(any(Long.class))).willReturn(Optional.empty());

        // order & orderDetail 설정
        User user = userRepository.findById(1L).orElseThrow(RuntimeException::new);

        order = orderRepository.findOrderById(1L);
        OrderDetail orderDetail = orderDetailRepository.findById(1L).orElseThrow(RuntimeException::new);

        ProductItem productItem = productItemRepository.findProductItemById(1L);
        Long beforeQuantity = productItem.getQuantity();

        // cart repo mocking
        doNothing().when(cartRepository).deleteById(any());

        // paymentService.processPayment mocking (분기)
        Payment payment = new Payment(1L,"NORMAL",order.getTotalAmount(),order.getPaymentId(),order.getTossOrderId(),order.getOrderedAt(),null,0,paymentStatus,order,user,0);
        given(paymentService.processPayment(any(Long.class), any(PaymentConfirmReqDto.class))).willReturn(payment);


        // OrderService ProcessPayment Parma setting
        DeliveryReqDto deliveryReqDto = new DeliveryReqDto(user.getName(),user.getCellPhone(),"11","11","11");


        orderServiceImpl = new OrderServiceImpl(orderRepository, orderDetailRepository, productItemRepository, userRepository, paymentRepository, deliveryRepository, addressRepository, reviewRepository, cartRepository, paymentService, aesUtil);


//        assertThatThrownBy(() ->
//                        orderServiceImpl.processPayment(
//                        user.getUserId(),
//                        order.getPaymentId(),
//                        order.getTossOrderId(),
//                        orderAmount,
//                        point,
//                        "NORMAL",
//                        deliveryReqDto
//                )).hasMessage(OrderError.PAYMENT_RETRY_REQUIRED.getMessage());
        //when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> orderServiceImpl.processPayment(
                        user.getUserId(),
                        order.getPaymentId(),
                        order.getTossOrderId(),
                        orderAmount,
                        point,
                        "NORMAL",
                        deliveryReqDto
                )
        );

        //then
        ProductItem productItem1 = productItemRepository.findProductItemById(1L);
        Long lastQuantity = productItem1.getQuantity();


        assertThat(exception.getMessage()).isEqualTo(OrderError.PAYMENT_RETRY_REQUIRED.getMessage());
        assertThat(exception.getHttpStatus()).isEqualTo(OrderError.PAYMENT_RETRY_REQUIRED.getStatus());
        assertThat(beforeQuantity-orderDetail.getQuantity()).isEqualTo(lastQuantity);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        System.out.println("Exception message: " + exception.getMessage());
        System.out.println("beforeQuantity = " + beforeQuantity);
        System.out.println("lastQuantity = " + lastQuantity);

    }

    @DisplayName("토스페이먼츠에서 5xx 응답을 보내주었을 때 응답을 재시도하고 예외를 리턴합니다. 주문은 PENDING 상태로 유지되고, 재고는 감소합니다.")
    @Test
    public void testOrderFor5xxResponse() {
        //given
        //user, product, product_item (sql)

        //결제 대기 (자동 재시도)
        PaymentStatus paymentStatus = PaymentStatus.APPROVED;

        // AESUtil Mocking
        MockedStatic<AESUtil> mockedStatic = mockStatic(AESUtil.class);
        mockedStatic.when(() -> aesUtil.decryptWithSalt(anyString()))
                .thenReturn("0");

        // 배송 mocking
        given(deliveryRepository.save(any())).willReturn(null);

        // paymentService findLatestPayment mocking
        given(paymentService.findLatestPayment(any(Long.class))).willReturn(Optional.empty());

        // order & orderDetail 설정
        User user = userRepository.findById(1L).orElseThrow(RuntimeException::new);

        order = orderRepository.findOrderById(1L);
        OrderDetail orderDetail = orderDetailRepository.findById(1L).orElseThrow(RuntimeException::new);

        ProductItem productItem = productItemRepository.findProductItemById(1L);
        Long beforeQuantity = productItem.getQuantity();

        // cart repo mocking
        doNothing().when(cartRepository).deleteById(any());

        // paymentService.processPayment mocking (분기)
        Payment payment = new Payment(1L,"NORMAL",order.getTotalAmount(),order.getPaymentId(),order.getTossOrderId(),order.getOrderedAt(),null,0,paymentStatus,order,user,0);
        given(paymentService.processPayment(any(Long.class), any(PaymentConfirmReqDto.class))).willReturn(payment);


        // OrderService ProcessPayment Parma setting
        DeliveryReqDto deliveryReqDto = new DeliveryReqDto(user.getName(),user.getCellPhone(),"11","11","11");


        orderServiceImpl = new OrderServiceImpl(orderRepository, orderDetailRepository, productItemRepository, userRepository, paymentRepository, deliveryRepository, addressRepository, reviewRepository, cartRepository, paymentService, aesUtil);

        //when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> orderServiceImpl.processPayment(
                        user.getUserId(),
                        order.getPaymentId(),
                        order.getTossOrderId(),
                        orderAmount,
                        point,
                        "NORMAL",
                        deliveryReqDto
                )
        );
        //then

        ProductItem productItem1 = productItemRepository.findProductItemById(1L);
        Long lastQuantity = productItem1.getQuantity();


        assertThat(exception.getMessage()).isEqualTo(OrderError.PAYMENT_RETRY_REQUIRED.getMessage());
        assertThat(exception.getHttpStatus()).isEqualTo(OrderError.PAYMENT_RETRY_REQUIRED.getStatus());
        assertThat(beforeQuantity-orderDetail.getQuantity()).isEqualTo(lastQuantity);
        System.out.println("Exception message: " + exception.getMessage());
        System.out.println("beforeQuantity = " + beforeQuantity);
        System.out.println("lastQuantity = " + lastQuantity);

    }

    @DisplayName("토스페이먼츠에서 2xx 응답을 보내주었을 때, 주문은 SUCCEEDED 되고 재고는 감소합니다.")
    @Test
    public void testOrderFor2xxResponse() {
        //given
        //user, product, product_item (sql), order, order_detail

        //결제 성공
        PaymentStatus paymentStatus = PaymentStatus.APPROVED;

        // AESUtil Mocking
        MockedStatic<AESUtil> mockedStatic = mockStatic(AESUtil.class);
        mockedStatic.when(() -> aesUtil.decryptWithSalt(anyString()))
                .thenReturn("0");

        // 배송 mocking
        given(deliveryRepository.save(any())).willReturn(null);

        // paymentService findLatestPayment mocking
        given(paymentService.findLatestPayment(any(Long.class))).willReturn(Optional.empty());

        // order & orderDetail 설정
        User user = userRepository.findById(1L).orElseThrow(RuntimeException::new);

        order = orderRepository.findOrderById(1L);
        OrderDetail orderDetail = orderDetailRepository.findById(1L).orElseThrow(RuntimeException::new);

        ProductItem productItem = productItemRepository.findProductItemById(1L);
        Long beforeQuantity = productItem.getQuantity();

        // cart repo mocking
        doNothing().when(cartRepository).deleteById(any());

        // paymentService.processPayment mocking (분기)
        Payment payment = new Payment(1L,"NORMAL",order.getTotalAmount(),order.getPaymentId(),order.getTossOrderId(),order.getOrderedAt(),null,0,paymentStatus,order,user,0);
        given(paymentService.processPayment(any(Long.class), any(PaymentConfirmReqDto.class))).willReturn(payment);


        // OrderService ProcessPayment Parma setting
        DeliveryReqDto deliveryReqDto = new DeliveryReqDto(user.getName(),user.getCellPhone(),"11","11","11");


        orderServiceImpl = new OrderServiceImpl(orderRepository, orderDetailRepository, productItemRepository, userRepository, paymentRepository, deliveryRepository, addressRepository, reviewRepository, cartRepository, paymentService,aesUtil);


        //when
        PaymentResDto paymentResDto = orderServiceImpl.processPayment(
                user.getUserId(),
                order.getPaymentId(),
                order.getTossOrderId(),
                orderAmount,
                point,
                "NORMAL",
                deliveryReqDto
        );
        //then

        ProductItem productItem1 = productItemRepository.findProductItemById(1L);
        Long lastQuantity = productItem1.getQuantity();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SUCCEEDED);
        System.out.println(order.getStatus());
        assertThat(beforeQuantity-orderDetail.getQuantity()).isEqualTo(lastQuantity);
        System.out.println("beforeQuantity = " + beforeQuantity);
        System.out.println("lastQuantity = " + lastQuantity);

    }

    @DisplayName("결제만 재시도하는 경우의 주문을 생성합니다. 주문은 SUCCEEDED 되고, 재고는 차감되지 않습니다. ")
    @Test
    void testRetryOrder(){

        //given
        //user, product, product_item (sql)

        //결제 성공
        PaymentStatus paymentStatus = PaymentStatus.APPROVED;

        // AESUtil Mocking
        MockedStatic<AESUtil> mockedStatic = mockStatic(AESUtil.class);
        mockedStatic.when(() -> aesUtil.decryptWithSalt(anyString()))
                .thenReturn("0");

        // 배송 mocking
        given(deliveryRepository.save(any())).willReturn(null);

        // order & orderDetail 설정
        User user = userRepository.findById(1L).orElseThrow(RuntimeException::new);

        order = orderRepository.findOrderById(1L);
        OrderDetail orderDetail = orderDetailRepository.findById(1L).orElseThrow(RuntimeException::new);

        ProductItem productItem = productItemRepository.findProductItemById(1L);
        Long beforeQuantity = productItem.getQuantity();

        // cart repo mocking
        doNothing().when(cartRepository).deleteById(any());

        // paymentService findLatestPayment mocking
        PaymentInfoResDto paymentInfoResDto = new PaymentInfoResDto(1L,"NORMAL",order.getTotalAmount(),order.getPointUsage(),paymentStatus,null,"NORMAL","1");
        given(paymentService.findLatestPayment(any(Long.class))).willReturn(Optional.of(paymentInfoResDto));

        // paymentService.processPayment mocking (분기)
        Payment payment = new Payment(1L,"NORMAL",order.getTotalAmount(),order.getPaymentId(),order.getTossOrderId(),order.getOrderedAt(),null,0,paymentStatus,order,user,0);
        given(paymentService.processPayment(any(Long.class), any(PaymentConfirmReqDto.class))).willReturn(payment);


        // OrderService ProcessPayment Parma setting
        DeliveryReqDto deliveryReqDto = new DeliveryReqDto(user.getName(),user.getCellPhone(),"11","11","11");


        orderServiceImpl = new OrderServiceImpl(orderRepository, orderDetailRepository, productItemRepository, userRepository, paymentRepository, deliveryRepository, addressRepository, reviewRepository, cartRepository, paymentService, aesUtil);


        //when
        PaymentResDto paymentResDto = orderServiceImpl.processPayment(
                user.getUserId(),
                order.getPaymentId(),
                order.getTossOrderId(),
                orderAmount,
                point,
                "NORMAL",
                deliveryReqDto
        );

        //then
        ProductItem productItem1 = productItemRepository.findProductItemById(1L);
        Long lastQuantity = productItem1.getQuantity();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SUCCEEDED);
        System.out.println(order.getStatus());
        assertThat(beforeQuantity).isEqualTo(lastQuantity);
        System.out.println("beforeQuantity = " + beforeQuantity);
        System.out.println("lastQuantity = " + lastQuantity);

    }
}
