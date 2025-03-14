package org.example.mollyapi.order.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.cart.repository.CartRepository;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.delivery.dto.DeliveryReqDto;
import org.example.mollyapi.delivery.repository.DeliveryRepository;
import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.order.entity.OrderDetail;
import org.example.mollyapi.order.repository.OrderDetailRepository;
import org.example.mollyapi.order.repository.OrderRepository;
import org.example.mollyapi.order.type.OrderStatus;
import org.example.mollyapi.payment.dto.request.PaymentConfirmReqDto;
import org.example.mollyapi.payment.dto.response.PaymentInfoResDto;
import org.example.mollyapi.payment.dto.response.PaymentResDto;
import org.example.mollyapi.payment.entity.Payment;
import org.example.mollyapi.payment.repository.PaymentRepository;
import org.example.mollyapi.payment.service.impl.PaymentServiceImpl;
import org.example.mollyapi.product.dto.UploadFile;
import org.example.mollyapi.product.entity.Product;
import org.example.mollyapi.product.entity.ProductImage;
import org.example.mollyapi.product.entity.ProductItem;
import org.example.mollyapi.product.repository.ProductImageRepository;
import org.example.mollyapi.product.repository.ProductItemRepository;
import org.example.mollyapi.product.repository.ProductRepository;
import org.example.mollyapi.product.service.ProductServiceImpl;
import org.example.mollyapi.review.repository.impl.ReviewCustomRepositoryImpl;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.example.mollyapi.user.type.Sex;
import org.example.mollyapi.payment.util.AESUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.example.mollyapi.order.entity.QOrderDetail.orderDetail;
import static org.example.mollyapi.payment.type.PaymentStatus.APPROVED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class OrderPaymentServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private OrderStockService orderStockService;

    @Autowired
    private ProductServiceImpl productService;

    @MockBean
    private PaymentServiceImpl paymentService;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    ProductImageRepository productImageRepository;

    @Autowired
    ProductItemRepository productItemRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @MockBean
    private ReviewCustomRepositoryImpl reviewCustomRepository;

    private User testUser;
    private Order testOrder;
    private static String encryptedPoint;
    private static MockedStatic<AESUtil> aesUtilMock;

    @BeforeAll
    public static void beforeAll() {
        // AESUtil Mocking
//        MockedStatic<AESUtil> mockedStatic = mockStatic(AESUtil.class);
//        mockedStatic.when(() -> AESUtil.decryptWithSalt(anyString()))
//                .thenReturn("0");
        aesUtilMock = mockStatic(AESUtil.class);
        aesUtilMock.when(() -> AESUtil.decryptWithSalt(anyString())).thenReturn("0"); // 기본값
    }

    @AfterAll
    public static void afterAll() {
        // 포인트 모킹 삭제
        if (aesUtilMock != null) {
            aesUtilMock.close();
        }    }

    @BeforeEach
    @Transactional
    void setup() {
        // 사용자 저장
        testUser = userRepository.save(User.builder()
                .name("test_user")
                .cellPhone("01012345678")
                .flag(true)
                .nickname("test_nickname")
                .sex(Sex.FEMALE)
                .point(1000)
                .build());

        // 주문 저장
        testOrder = orderRepository.save(new Order(testUser, "ORD-202503111234-5678"));
        testOrder.updateTotalAmount(5000L);
        testOrder.updateStatus(OrderStatus.PENDING);

        Product product = createTestProduct("Nike", 10000L);
        ProductItem productItem = createTestProductItem(product, "Red", "L", 5L);
        OrderDetail orderDetail = createTestOrderDetail(testOrder, productItem, 2L);

        orderRepository.save(testOrder);
        orderDetailRepository.save(orderDetail);

        System.out.println("테스트 셋업 완료 - User ID: " + testUser.getUserId());

        Optional<Order> foundOrder = orderRepository.findByTossOrderIdWithDetails("ORD-202503111234-5678");
        System.out.println("🔎 주문 조회 결과: " + foundOrder.isPresent());
    }

    @AfterEach
    void cleanup() {
        // 결제, 주문 상세, 장바구니 삭제
        cartRepository.deleteAllInBatch();
        orderDetailRepository.deleteAllInBatch();
        paymentRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        deliveryRepository.deleteAllInBatch();

        // 상품 관련 데이터 삭제
        productImageRepository.deleteAllInBatch();
        productItemRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();

        // 유저 데이터 삭제
        userRepository.deleteAllInBatch();
    }

    private Product createTestProduct(String brand, Long price){
        Product product = Product.builder()
                .brandName(brand)
                .price(price)
                .build();
        Product savedProduct = productRepository.save(product);

        MockMultipartFile mockfile = new MockMultipartFile(
                "file",
                "test-file.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{1, 2, 3, 4}
        );
        productService.registerProductImages(savedProduct, List.of(mockfile));

        // 대표 이미지 추가
        ProductImage representativeImage = ProductImage.createThumbnail(savedProduct,
                UploadFile.builder()
                        .storedFileName("/images/product/test-thumbnail.png")
                        .uploadFileName("test-thumbnail.png")
                        .build()
        );
        savedProduct.addImage(representativeImage);
        return productRepository.save(savedProduct);
    }

    private ProductItem createTestProductItem(Product product, String color, String size, Long quantity) {
        ProductItem productItem = ProductItem.builder()
                .color(color)
                .size(size)
                .quantity(quantity)
                .product(product)
                .build();
        return productItemRepository.save(productItem);
    }

    private OrderDetail createTestOrderDetail(Order order, ProductItem productItem, Long quantity) {
        OrderDetail orderDetail = new OrderDetail(order, productItem, productItem.getSize(),
                productItem.getProduct().getPrice(), quantity,
                productItem.getProduct().getBrandName(), productItem.getProduct().getProductName(), 100L);
        order.getOrderDetails().add(orderDetail);
        return orderDetailRepository.save(orderDetail);
    }

    private DeliveryReqDto createTestDeliveryInfo() {
        return new DeliveryReqDto(
                "momo", "010-1111-2222", "판교", "12345", "배송 조심히 해주세요"
        );
    }


    @Test
    @DisplayName("결제 요청 전에 모든 검증이 정상적으로 수행된다.")
    void processPayment_PreValidationSuccess(){
        /// given
        String mockTossOrderId = "mockTossOrderId";
        Long userId = testUser.getUserId();
        String paymentKey = "test-key";
        String tossOrderId = testOrder.getTossOrderId();
        Long amount = 5000L;
        String paymentType = "CREDIT_CARD";
        DeliveryReqDto deliveryInfo = new DeliveryReqDto("momo", "010-1111-2222", "판교", "12345", "배송 조심히 해주세요");
        Payment mockPayment = Payment.create(testUser, testOrder, mockTossOrderId, paymentKey, paymentType, amount);

        PaymentConfirmReqDto paymentConfirmReqDto = new PaymentConfirmReqDto(
                testOrder.getId(),
                testOrder.getTossOrderId(),
                testOrder.getPaymentId(),
                testOrder.getTotalAmount(),
                testOrder.getPaymentType(),
                testOrder.getPointUsage()
        );
        String point = "500"; // 사용할 포인트

        when(paymentService.processPayment(testUser.getUserId(), paymentConfirmReqDto)).thenReturn(mockPayment);

        /// when
        PaymentResDto resDto = orderService.processPayment(userId, paymentKey, tossOrderId, amount, encryptedPoint, paymentType, deliveryInfo);

        /// then // PaymentConfirmReqDto 검증
        assertThat(resDto).isNotNull()
                        .extracting("amount", "paymentType","tossOrderId")
                                .containsExactly(amount, paymentType, mockTossOrderId);
    }

    @Test
//    @Transactional
    @DisplayName("결제가 성공하면 결제 정보를 업데이트 하고 주문 상태를 변경한다")
    public void processPayment_PaymentSuccess() {
        /// Given
        String mockTossOrderId = "ORD-202503111234-5678";
        Long userId = testUser.getUserId();
        String paymentKey = "test-key";
        String tossOrderId = testOrder.getTossOrderId();
        Long amount = 5000L;
        String paymentType = "CREDIT_CARD";
        DeliveryReqDto deliveryInfo = new DeliveryReqDto("momo", "010-1111-2222", "판교", "12345", "배송 조심히 해주세요");
        Payment mockPayment = Payment.create(testUser, testOrder, mockTossOrderId, paymentKey, paymentType, amount);

        PaymentConfirmReqDto paymentConfirmReqDto = new PaymentConfirmReqDto(
                testOrder.getId(),
                testOrder.getTossOrderId(),
                testOrder.getPaymentId(),
                testOrder.getTotalAmount(),
                testOrder.getPaymentType(),
                testOrder.getPointUsage()
        );
        String point = "500"; // 사용할 포인트

        when(paymentService.processPayment(testUser.getUserId(), paymentConfirmReqDto)).thenReturn(mockPayment);

        /// when
        PaymentResDto resDto = orderService.processPayment(userId, paymentKey, tossOrderId, amount, encryptedPoint, paymentType, deliveryInfo);

        ///then
        Order vaildOrder = orderRepository.findByTossOrderId(mockTossOrderId).get();
        assertThat(vaildOrder).isNotNull()
                .extracting("status", "paymentAmount")
                .containsExactly(OrderStatus.SUCCEEDED, amount);

    }

    @Test
    @DisplayName("존재하지 않는 주문 ID로 결제 요청 시 예외 발생")
    void processPayment_OrderNotFound_ShouldThrowException() {
        /// given
        Long invalidOrderId = 999L;
        Long userId = testUser.getUserId();
        String paymentKey = "test-key";
        Long amount = 5000L;
        String paymentType = "CREDIT_CARD";
        DeliveryReqDto deliveryInfo = new DeliveryReqDto("momo", "010-1111-2222", "판교판교", "12345", "배송 조심히 해주세요");

        /// when & then
        assertThatThrownBy(() -> orderService.processPayment(userId, paymentKey, "invalid-order-id", amount, encryptedPoint, paymentType, deliveryInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 주문을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("결제 금액이 다를 경우 예외 발생")
    void processPayment_InvalidAmount_ShouldThrowException() {
        /// given
        Long userId = testUser.getUserId();
        String paymentKey = "test-key";
        String tossOrderId = testOrder.getTossOrderId();
        Long wrongAmount = 9999L;
        String paymentType = "CREDIT_CARD";
        DeliveryReqDto deliveryInfo = new DeliveryReqDto("momo", "010-1111-2222", "판교판교", "12345", "배송 조심히 해주세요");

        /// when & then
        assertThatThrownBy(() -> orderService.processPayment(userId, paymentKey, tossOrderId, wrongAmount, encryptedPoint, paymentType, deliveryInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("결제 금액이 일치하지 않습니다.");
    }

    @Test
    @DisplayName("포인트가 부족한 경우 예외 발생")
    void processPayment_InsufficientPoints_ShouldThrowException() {
        /// given
        testUser.updatePoint(-1000); // 포인트 0으로 만듦
        userRepository.save(testUser);

        Long userId = testUser.getUserId();
        String paymentKey = "test-key";
        String tossOrderId = testOrder.getTossOrderId();
        Long amount = 5000L;
        String paymentType = "CREDIT_CARD";
        DeliveryReqDto deliveryInfo = new DeliveryReqDto("momo", "010-1111-2222", "판교판교", "12345", "배송 조심히 해주세요");
        String point = "1500";

        // AESUtil.decryptWithSalt() Mocking 변경
        aesUtilMock.when(() -> AESUtil.decryptWithSalt(anyString())).thenReturn("1500");

        /// when & then
        assertThatThrownBy(() -> orderService.processPayment(userId, paymentKey, tossOrderId, amount, point, paymentType, deliveryInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("포인트가 부족합니다.");

    }

    @Test
    @DisplayName("결제 정보 누락 시 예외 발생")
    void processPayment_MissingPaymentInfo_ShouldThrowException() {
        /// given
        Long userId = testUser.getUserId();
        String paymentKey = ""; // 결제 정보 미입력
        String tossOrderId = testOrder.getTossOrderId();
        Long amount = 5000L;
        String paymentType = "CREDIT_CARD";
        DeliveryReqDto deliveryInfo = new DeliveryReqDto("momo", "010-1111-2222", "판교", "12345", "배송 조심히 해주세요");

        /// when & then
        assertThatThrownBy(() -> orderService.processPayment(userId, paymentKey, tossOrderId, amount, encryptedPoint, paymentType, deliveryInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("결제 정보가 누락되었습니다.");
    }

    @Test
    @DisplayName("배송 정보 누락 시 예외 발생 - 수신자 이름")
    void processPayment_MissingDeliveryInfo_ShouldThrowException() {
        /// given
        Long userId = testUser.getUserId();
        String paymentKey = "test-key";
        String tossOrderId = testOrder.getTossOrderId();
        Long amount = 5000L;
        String paymentType = "CREDIT_CARD";

        // 배송 정보 일부 필드를 null로 설정
        DeliveryReqDto deliveryInfo = new DeliveryReqDto(
                null, // 수신자 이름 누락
                "010-1111-2222",
                "판교",
                "12345",
                "배송 조심히 해주세요"
        );

        /// when & then
        assertThatThrownBy(() -> orderService.processPayment(userId, paymentKey, tossOrderId, amount, encryptedPoint, paymentType, deliveryInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("배송 정보가 누락되었습니다.");
    }

    @Test // ❗️delivery persistenceTest로 보내기
    @DisplayName("배송 정보 누락 시 예외 발생 - 배송지 번호 누락")
    void processPayment_MissingReceiverPhone_ShouldThrowException() {
        /// given
        DeliveryReqDto deliveryInfo = new DeliveryReqDto(
                "momo",
                null, // 착신자 번호 누락
                "판교",
                "12345",
                "배송 조심히 해주세요"
        );

        /// when & then
        assertThatThrownBy(() -> deliveryInfo.validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("배송 정보가 누락되었습니다.");
    }

    @Test // ❗️delivery persistenceTest로 보내기
    @DisplayName("배송 정보 누락 시 예외 발생 - 도로명 주소 누락")
    void processPayment_MissingRoadAddress_ShouldThrowException() {
        /// given
        DeliveryReqDto deliveryInfo = new DeliveryReqDto(
                "momo",
                "010-1111-2222",
                null, // 도로명 주소 누락
                "12345",
                "배송 조심히 해주세요"
        );

        /// when & then
        assertThatThrownBy(() -> deliveryInfo.validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("배송 정보가 누락되었습니다.");
    }

    @Test
    @DisplayName("결제 요청 시 상품의 재고가 결제 시도 전에 미리 차감된다.")
    void validateBeforePayment_SufficientStock() {
        ///given
        Product testProduct = createTestProduct("adidas", 5000L);
        ProductItem testProductItem = createTestProductItem(testProduct, "blue", "M", 5L);
        OrderDetail orderDetail = createTestOrderDetail(testOrder, testProductItem, 2L);
        DeliveryReqDto deliveryInfo = createTestDeliveryInfo();

        /// when
        orderStockService.validateBeforePayment(testOrder.getId());

        /// then
        // 상품의 재고가 감소했는지 검증
        ProductItem updatedProductItem = productItemRepository.findById(testProductItem.getId()).orElseThrow();
        assertThat(updatedProductItem.getQuantity()).isEqualTo(3L); // 5 → 3개로 감소해야 함

        // ❗이건 레포지토리 테스트로 빼야될듯
//        // `decreaseStock()`이 실행되었는지 검증
//        verify(productItemRepository, times(1)).save(any(ProductItem.class));
    }

    @Test
    @DisplayName("결제 전에 재고를 검증하고 차감할 때, 재고가 부족하면 예외가 발생해야 한다.")
    void validateBeforePayment_InsufficientStock_ShouldThrowException() {
        ///given
        Product testProduct = createTestProduct("adidas", 5000L);
        ProductItem testProductItem = createTestProductItem(testProduct, "blue", "M", 5L);
        OrderDetail orderDetail = createTestOrderDetail(testOrder, testProductItem, 6L);
        DeliveryReqDto deliveryInfo = createTestDeliveryInfo();

        /// when & then
        assertThatThrownBy(() -> orderStockService.validateBeforePayment(testOrder.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("재고가 부족하여 결제를 진행할 수 없습니다.");
    }

    /// OrderDetail->ProductItem을 참조하므로 상황을 억지로 만들어주지 않는 이상 예외가 발생하지 않음
//    @Test
//    @DisplayName("결제 전에 재고를 검증할 때, 상품이 존재하지 않으면 예외가 발생해야 한다.")
//    void validateBeforePayment_ProductNotFound_ShouldThrowException() {
//        /// given
//        Product testProduct = createTestProduct("adidas", 5000L);
//        ProductItem testProductItem = createTestProductItem(testProduct, "blue", "M", 5L);
//        OrderDetail orderDetail = createTestOrderDetail(testOrder, testProductItem, 1L);
//        DeliveryReqDto deliveryInfo = createTestDeliveryInfo();
//
//        /// when: 상품 삭제 후 결제 시도
//        productItemRepository.deleteById(testProductItem.getId()); // 상품을 데이터베이스에서 하드 삭제
//
//        log.info("orderDetailRepository Size = {}", orderDetailRepository.count());
//        log.info("productItemRepository = {}", productItemRepository.count());
//
//        /// then: 상품이 존재하지 않으므로 예외 발생해야 함
//        assertThatThrownBy(() -> orderStockService.validateBeforePayment(testOrder.getId()))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("상품을 찾을 수 없습니다. itemId=" + orderDetail.getProductItem().getId());
//    }

    @Test
    @DisplayName("[경합] 비관적 락이 걸려서 동시에 주문 요청이 와도 재고가 안전하게 차감된다")
    @Transactional
    void testPessimisticLockConcurrency() throws InterruptedException {
        /// given
        Product testProduct = createTestProduct("adidas", 5000L);
        ProductItem testProductItem = createTestProductItem(testProduct, "blue", "M", 5L);

        /// when
        int threadCount = 10; // 동시에 5개의 주문 요청
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            executorService.execute(() -> {
                try {
                    log.info("{}번째 쓰레드 접근 ", finalI);
                    orderStockService.validateBeforePayment(testOrder.getId());
                    successCount.getAndIncrement();
                    log.info("{}번째 쓰레드 성공 ", finalI);
                } catch(Exception e){
                    failCount.getAndIncrement();
                    log.info("{}번쨰 쓰레드 실패 ", finalI);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드가 끝날 때까지 대기

        /// then
        // 최종 재고 확인 (동시에 5개 주문했으므로 5개 감소해야 함)
        ProductItem updatedProduct = productItemRepository.findById(testProductItem.getId()).orElseThrow();
        assertThat(updatedProduct.getQuantity()).isEqualTo(5L); // 10 → 5로 감소해야 함
    }

    /// orderService.validateBeforePayment 수정 필
//    @Test
//    @DisplayName("[동시성] 동시성 이슈로 인해 재고가 부족한 경우 예외를 발생시킨다")
//    @Transactional
//    void testPessimisticLock_InsufficientStock_ShouldThrowException() throws InterruptedException {
//        /// given
//        Product testProduct = createTestProduct("adidas", 5000L);
//        ProductItem testProductItem = createTestProductItem(testProduct, "blue", "M", 5L);
//
//        /// when
//        int threadCount = 6; // 6개 주문 (재고는 10개라서 일부는 실패해야 함)
//        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
//        CountDownLatch latch = new CountDownLatch(threadCount);
//
//        for (int i = 0; i < threadCount; i++) {
//            executorService.submit(() -> {
//                try {
//                    orderService.validateBeforePayment(null, testOrder, "0", null);
//                } catch (Exception e) {
//                    System.out.println("예외 발생: " + e.getMessage());
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        latch.await(); // 모든 스레드 종료 대기
//
//        /// then
//        // 최종 재고 확인 (일부 주문이 실패했어야 함)
//        ProductItem updatedProduct = productItemRepository.findById(testProductItem.getId()).orElseThrow();
//        assertThat(updatedProduct.getQuantity()).isGreaterThanOrEqualTo(0); // 음수가 되면 안됨
//    }


//    @Test
//    @Transactional
//    @DisplayName("[데드락] 동일 상품에 대해 동시에 결제 시도할 경우 데드락이 발생하지 않고 한쪽이 실패해야 한다")
//    void concurrentPayment_ShouldHandleLockingCorrectly() throws InterruptedException {
//        /// given
//        User testUser1 = userRepository.save(User.builder()
//                .name("test_user_1")
//                .cellPhone("01012345678")
//                .flag(true)
//                .nickname("test_nickname_1")
//                .sex(Sex.FEMALE)
//                .point(1000)
//                .build());
//
//        User testUser2 = userRepository.save(User.builder()
//                .name("test_user_2")
//                .cellPhone("01012345678")
//                .flag(true)
//                .nickname("test_nickname_2")
//                .sex(Sex.FEMALE)
//                .point(1000)
//                .build());
//
//        Product product = createTestProduct("adidas", 5000L);
//        ProductItem productItem = createTestProductItem(product, "blue", "M", 2L); // 재고 2개
//
//        Order testOrder1 = orderRepository.save(new Order(testUser1, "ORD-202511111111-1234"));
//        testOrder1.updateTotalAmount(5000L);
//        testOrder1.updateStatus(OrderStatus.PENDING);
//        orderRepository.save(testOrder1);
//
//        Order testOrder2 = orderRepository.save(new Order(testUser2, "ORD-202511111111-5678"));
//        testOrder2.updateTotalAmount(5000L);
//        testOrder2.updateStatus(OrderStatus.PENDING);
//        orderRepository.save(testOrder2);
//
//        DeliveryReqDto deliveryInfo = new DeliveryReqDto(
//                "momo", "010-1111-2222", "판교", "12345", "배송 조심히 해주세요"
//        );
//
//        /// when - 두 사용자가 동시에 결제를 요청하도록 멀티스레드로 실행
//        ExecutorService executor = Executors.newFixedThreadPool(2);
//
//        Future<?> futureA = executor.submit(() -> {
//            orderService.validateBeforePayment(testUser1, testOrder1, "500", deliveryInfo);
//        });
//
//        Future<?> futureB = executor.submit(() -> {
//            orderService.validateBeforePayment(testUser2, testOrder2, "500", deliveryInfo);
//        });
//
//        executor.shutdown();
//        executor.awaitTermination(5, TimeUnit.SECONDS);
//
//        try {
//            futureA.get();
//        } catch (Exception e) {
//            System.out.println("futureA 예외 발생: " + e.getMessage());
//        }
//
//        try {
//            futureB.get();
//        } catch (Exception e) {
//            System.out.println("futureB 예외 발생: " + e.getMessage());
//        }
//
//        /// then
//        assertThatThrownBy(futureB::get)
//                .hasCauseInstanceOf(CustomException.class)
//                .hasMessageContaining("재고가 부족하여 결제를 진행할 수 없습니다.");
//
//        // 확인: testOrder1은 성공해야 하고, testOrder2는 실패해야 함
//        ProductItem updatedProductItem = productItemRepository.findById(productItem.getId()).orElseThrow();
//        assertThat(updatedProductItem.getQuantity()).isEqualTo(1L); // A가 1개 차감 후 남은 재고 1개
//    }

    @Test
    @DisplayName("동일한 주문 ID에 대해 중복 결제 요청 시 예외 발생")
    void processPayment_DuplicatePaymentRequest_ShouldThrowException() {
        /// given
        Long userId = testUser.getUserId();
        String paymentKey = "test-key";
        String tossOrderId = testOrder.getTossOrderId();
        Long amount = 5000L;
        String paymentType = "CREDIT_CARD";
        DeliveryReqDto deliveryInfo = new DeliveryReqDto("momo", "010-1111-2222", "판교", "12345", "배송 조심히 해주세요");
        String point = "500";

        // 기존 PaymentInfoResDto가 아닌 Payment 객체를 반환하도록 변경
        Payment mockPayment = Payment.create(
                testUser,  // 사용자
                testOrder, // 주문
                tossOrderId, // Toss 주문 ID
                paymentKey,  // 결제 키
                paymentType, // 결제 타입
                amount      // 결제 금액
        );

        when(paymentService.findLatestPayment(any()))
                .thenReturn(Optional.of(PaymentInfoResDto.from(mockPayment)));
        /// when & then
        assertThatThrownBy(() -> orderService.processPayment(userId, paymentKey, tossOrderId, amount, encryptedPoint, paymentType, deliveryInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 결제된 주문입니다.");
    }
}