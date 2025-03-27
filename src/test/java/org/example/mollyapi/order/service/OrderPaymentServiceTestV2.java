//package org.example.mollyapi.order.service;
//
//import lombok.extern.slf4j.Slf4j;
//import org.example.mollyapi.address.repository.AddressRepository;
//import org.example.mollyapi.cart.repository.CartRepository;
//import org.example.mollyapi.common.exception.CustomException;
//import org.example.mollyapi.common.exception.error.impl.OrderError;
//import org.example.mollyapi.delivery.dto.DeliveryReqDto;
//import org.example.mollyapi.delivery.repository.DeliveryRepository;
//import org.example.mollyapi.order.entity.Order;
//import org.example.mollyapi.order.entity.OrderDetail;
//import org.example.mollyapi.order.repository.OrderDetailRepository;
//import org.example.mollyapi.order.repository.OrderRepository;
//import org.example.mollyapi.order.type.OrderStatus;
//import org.example.mollyapi.payment.dto.response.PaymentResDto;
//import org.example.mollyapi.payment.entity.Payment;
//import org.example.mollyapi.payment.exception.RetryablePaymentException;
//import org.example.mollyapi.payment.repository.PaymentRepository;
//import org.example.mollyapi.payment.service.impl.PaymentServiceImpl;
//import org.example.mollyapi.payment.type.PaymentStatus;
//import org.example.mollyapi.payment.util.AESUtil;
//import org.example.mollyapi.product.entity.ProductItem;
//import org.example.mollyapi.product.repository.CategoryRepository;
//import org.example.mollyapi.product.repository.ProductItemRepository;
//import org.example.mollyapi.product.repository.ProductRepository;
//import org.example.mollyapi.review.repository.ReviewRepository;
//import org.example.mollyapi.user.entity.User;
//import org.example.mollyapi.user.repository.UserRepository;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.MockedStatic;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.SpyBean;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.jdbc.Sql;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
//@SpringBootTest
//@Slf4j
////@Transactional
//@ActiveProfiles("test")
//@Sql(scripts = "/setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
//public class OrderPaymentServiceTestV2 {
//
//    @Autowired
//    UserRepository userRepository;
//    @Autowired
//    OrderRepository orderRepository;
//    @Autowired
//    ProductItemRepository productItemRepository;
//    @Autowired
//    OrderDetailRepository orderDetailRepository;
//
//    @Autowired
//    DeliveryRepository deliveryRepository;
//    @Autowired
//    ProductRepository productRepository;
//
//    User user;
//    Order order;
//    Long orderAmount = 50000L;
//    String point = "0";
//
//    @Autowired
//    CategoryRepository categoryRepository;
//    @Autowired
//    CartRepository cartRepository;
//    @Autowired
//    PaymentRepository paymentRepository;
//    @Autowired
//    AddressRepository addressRepository;
//    @Autowired
//    ReviewRepository reviewRepository;
//    @Autowired
//    OrderStockService orderStockService;
//
//    @SpyBean
//    PaymentServiceImpl paymentServiceImpl;
//
//    @Autowired
//    OrderServiceImpl orderServiceImpl;
//
//
//    @BeforeAll
//    public static void beforeAll() {
//        // AESUtil Mocking
//        MockedStatic<AESUtil> mockedStatic = mockStatic(AESUtil.class);
//        mockedStatic.when(() -> AESUtil.decryptWithSalt(anyString()))
//                .thenReturn("0");
//    }
//
//    @AfterEach
//    public void tearDown() {
//        order.setDelivery(null);
//        orderRepository.save(order);
//        deliveryRepository.deleteById(1L);
//        orderRepository.deleteAll();
//        cartRepository.deleteAll();
//        productRepository.deleteAll();
//        productItemRepository.deleteAll();
//        categoryRepository.deleteAll();
//        userRepository.deleteAll();
//    }
//
//    @DisplayName("토스페이먼츠 4xx 응답 : 예외를 리턴, 재고 감소, 주문 pending\n")
//    @Test
//    public void testOrderFor4xxResponse() {
//        //given
//
//        // order & orderDetail 설정
//        user = userRepository.findById(1L).orElseThrow(RuntimeException::new);
//        order = orderRepository.findById(1L).orElseThrow(RuntimeException::new);
//        OrderDetail orderDetail = orderDetailRepository.findById(1L).orElseThrow(RuntimeException::new);
//
//        ProductItem productItem = productItemRepository.findWithOutLById(1L)
//                .orElseThrow(RuntimeException::new);
//        Long beforeQuantity = productItem.getQuantity();
//
//        // OrderService ProcessPayment Param setting
//        DeliveryReqDto deliveryReqDto = new DeliveryReqDto("momo","01015151515","11","11","11");
//
//        // 결제 Mocking
//        PaymentStatus paymentStatus = PaymentStatus.FAILED;
//        Payment expectedPayment = new Payment(
//                1L,"NORMAL",order.getTotalAmount(),order.getPaymentId(),order.getTossOrderId(),order.getOrderedAt(),null,0,paymentStatus,order,user
//        );
//        doThrow(new CustomException(OrderError.PAYMENT_RETRY_REQUIRED))
//                .when(paymentServiceImpl)
//                .processPayment(anyLong(), any());
//
//        // when & then
//
//        // 에러 검증
//        assertThatThrownBy(() ->
//                orderServiceImpl.processPayment(
//                        user.getUserId(),
//                        order.getPaymentId(),
//                        order.getTossOrderId(),
//                        orderAmount,
//                        point,
//                        "NORMAL",
//                        deliveryReqDto
//                )).hasMessage(OrderError.PAYMENT_RETRY_REQUIRED.getMessage());
//
//        //재고 검증
//        Long lastQuantity = beforeQuantity - orderDetail.getQuantity();
//        assertThat(beforeQuantity-orderDetail.getQuantity()).isEqualTo(lastQuantity);
//
//        //order 상태 검증
//        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
//    }
//
//    @DisplayName("토스페이먼츠 2xx 응답 : 재고 감소, 주문 succeeded, 결제 응답 Approved")
//    @Test
//    public void testOrderFor2xxResponse() {
//        //given
//
//        // order & orderDetail 설정
//        User user = userRepository.findById(1L).orElseThrow(RuntimeException::new);
//        order = orderRepository.findOrderById(1L);
//        OrderDetail orderDetail = orderDetailRepository.findById(1L).orElseThrow(RuntimeException::new);
//        ProductItem productItem = productItemRepository.findWithOutLById(1L)
//                .orElseThrow(RuntimeException::new);
//        Long beforeQuantity = productItem.getQuantity();
//
//        // OrderService ProcessPayment Param setting
//        DeliveryReqDto deliveryReqDto = new DeliveryReqDto(user.getName(),user.getCellPhone(),"11","11","11");
//
//        // 결제 Mocking
//        PaymentStatus paymentStatus = PaymentStatus.APPROVED;
//        Payment expectedPayment = new Payment(
//                1L,"NORMAL",order.getTotalAmount(),order.getPaymentId(),order.getTossOrderId(),order.getOrderedAt(),null,0,paymentStatus,order,user
//        );
//        doReturn(expectedPayment)
//                .when(paymentServiceImpl)
//                .processPayment(anyLong(), any());
//
//        // when
//        PaymentResDto paymentResDto = orderServiceImpl.processPayment(
//                user.getUserId(),
//                order.getPaymentId(),
//                order.getTossOrderId(),
//                orderAmount,
//                point,
//                "NORMAL",
//                deliveryReqDto
//        );
//
//
//        // then
//
//        // payment 응답 검증
//        assertThat(paymentResDto)
//                .extracting(PaymentResDto::paymentStatus,PaymentResDto::tossOrderId)
//                .containsExactlyInAnyOrder("결제승인", order.getTossOrderId());
//
//        // 재고 검증
//        Long lastQuantity = beforeQuantity - orderDetail.getQuantity();
//        assertThat(beforeQuantity-orderDetail.getQuantity()).isEqualTo(lastQuantity);
//
//        // order succeeded 상태 검증
//        order = orderRepository.findOrderById(order.getId());
//        assertThat(order.getStatus()).isEqualTo(OrderStatus.SUCCEEDED);
//    }
//
//    @DisplayName("토스페이먼츠 5xx 응답(재시도 후 실패) : 예외 리턴, 재고 감소, 주문 pending 합니다.\n")
//    @Test
//    public void testOrderFor5xxResponse() {
//        //given
//
//        // order & orderDetail 설정
//        User user = userRepository.findById(1L).orElseThrow(RuntimeException::new);
//
//        order = orderRepository.findOrderById(1L);
//        OrderDetail orderDetail = orderDetailRepository.findById(1L).orElseThrow(RuntimeException::new);
//
//        ProductItem productItem = productItemRepository.findWithOutLById(1L)
//                .orElseThrow(RuntimeException::new);
//        Long beforeQuantity = productItem.getQuantity();
//
//        // OrderService ProcessPayment Param setting
//        DeliveryReqDto deliveryReqDto = new DeliveryReqDto(user.getName(),user.getCellPhone(),"11","11","11");
//
//        // 결제 Mocking
//        PaymentStatus paymentStatus = PaymentStatus.PENDING;
//        Payment expectedPayment = new Payment(
//                1L,"NORMAL",order.getTotalAmount(),order.getPaymentId(),order.getTossOrderId(),order.getOrderedAt(),null,0,paymentStatus,order,user
//        );
//        doThrow(new RetryablePaymentException("서버 내부 오류"))
//                .when(paymentServiceImpl)
//                .processPayment(anyLong(), any());
//
//        // when & then
//        // 에러 검증
//        assertThatThrownBy(() ->
//                orderServiceImpl.processPayment(
//                        user.getUserId(),
//                        order.getPaymentId(),
//                        order.getTossOrderId(),
//                        orderAmount,
//                        point,
//                        "NORMAL",
//                        deliveryReqDto
//                )).isInstanceOf(RetryablePaymentException.class)
//                .hasMessage("서버 내부 오류");
//
//
//        //재고 검증
//        Long lastQuantity = beforeQuantity - orderDetail.getQuantity();
//        assertThat(beforeQuantity-orderDetail.getQuantity()).isEqualTo(lastQuantity);
//        //order 상태 검증
//        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
//    }
//
//}
//
