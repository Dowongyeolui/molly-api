package org.example.mollyapi.order.service;

import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.cart.entity.Cart;
import org.example.mollyapi.cart.repository.CartRepository;
import org.example.mollyapi.order.dto.OrderDetailResponseDto;
import org.example.mollyapi.order.dto.OrderRequestDto;
import org.example.mollyapi.order.dto.OrderResponseDto;
import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.order.entity.OrderDetail;
import org.example.mollyapi.order.repository.OrderDetailRepository;
import org.example.mollyapi.order.repository.OrderRepository;
import org.example.mollyapi.order.type.OrderStatus;
import org.example.mollyapi.product.dto.UploadFile;
import org.example.mollyapi.product.entity.Product;
import org.example.mollyapi.product.entity.ProductImage;
import org.example.mollyapi.product.entity.ProductItem;
import org.example.mollyapi.product.repository.ProductImageRepository;
import org.example.mollyapi.product.repository.ProductItemRepository;
import org.example.mollyapi.product.repository.ProductRepository;
import org.example.mollyapi.product.service.ProductServiceImpl;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.example.mollyapi.user.type.Sex;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class OrderCreateServiceTest {

    @Autowired
    OrderServiceImpl orderService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderDetailRepository orderDetailRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductItemRepository productItemRepository;

    @Autowired
    ProductServiceImpl productService;

    @Autowired
    ProductImageRepository productImageRepository;

    private User savedUser;
    private Product savedProduct;
    private ProductItem savedProductItem;

    @BeforeEach
    void setup(){
        User user = User.builder()
                .name("test_user")
                .cellPhone("01012345678")
                .flag(true)
                .nickname("test_nickname")
                .sex(Sex.FEMALE)
                .point(1000)
                .build();
        savedUser = userRepository.save(user);

        Product product = Product.builder()
                .user(savedUser)
                .brandName("adidas")
                .price(5000L)
                .build();
        savedProduct = productRepository.save(product);

        MockMultipartFile mockfile = new MockMultipartFile(
                "file",
                "test-file.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{1, 2, 3, 4}
        );
        productService.registerProductImages(savedProduct, List.of(mockfile));

        // 대표 이미지 강제 추가
        ProductImage representativeImage = ProductImage.createThumbnail(savedProduct,
                UploadFile.builder()
                        .storedFileName("/images/product/test-thumbnail.png")
                        .uploadFileName("test-thumbnail.png")
                        .build()
        );
        savedProduct.addImage(representativeImage);
        savedProduct = productRepository.save(savedProduct);

        ProductItem productItem = ProductItem.builder()
                .color("blue")
                .size("M")
                .quantity(5L) // 재고 5개
                .product(savedProduct)
                .build();
        savedProductItem = productItemRepository.save(productItem);
    }

    @AfterEach
    void cleanUpDatabase() {
        cartRepository.deleteAllInBatch();
        orderDetailRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        productImageRepository.deleteAllInBatch();
        productItemRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }


    @Test
    @DisplayName("주문을 요청하면 사용자 조회, tossOrderId 생성, 주문 및 주문 상세 생성, 주문 금액 계산 및 업데이트, 기본 배송지 조회, 응답 생성 및 반환을 검증한다.")
    void createOrderTest_Success() {
        /// given
        Cart cart = Cart.builder()
                .user(savedUser)
                .quantity(2L) // 카트에 담긴 수량 : 2개
                .productItem(savedProductItem)
                .build();
        cartRepository.save(cart);

        OrderRequestDto orderReq = OrderRequestDto.builder()
                .cartId(cart.getCartId())
                .itemId(savedProductItem.getId())
                .quantity(2L)  // 2개 주문
                .build();

        /// when
        OrderResponseDto orderResponse = orderService.createOrder(savedUser.getUserId(), List.of(orderReq));

        /// then
        // ❓비교 #1
        assertThat(orderResponse)
                .isNotNull()
                .extracting(
                        OrderResponseDto::status,
                        OrderResponseDto::totalAmount
                )
                .containsExactly(
                        OrderStatus.PENDING,
                        5000L * 2
                );

        assertThat(orderResponse.orderDetails())
                .hasSize(1) // 주문 상세가 1개인지 검증
                .first() // 첫 번째(유일한) 주문 상세만 검증
                .extracting(
                        OrderDetailResponseDto::brandName,
                        OrderDetailResponseDto::quantity
                )
                .containsExactly(
                        "adidas",
                        2L
                );

        assertThat(orderResponse.tossOrderId())
                .isNotEmpty()
                .matches("^ORD-\\d{14}-\\d{1,4}$"); // tossOrderId 생성 규칙 "ORD-YYYYMMDDHHmmss-랜덤숫자" 패턴 검증


        // ❓비교 #2
        assertThat(orderResponse)
                .isNotNull()
                .extracting("status", "totalAmount", "tossOrderId")
                .containsExactly(OrderStatus.PENDING, 5000L * 2, orderResponse.tossOrderId());

        assertThat(orderResponse.tossOrderId())
                .as("Toss Order ID 형식 검증")
                .matches("^ORD-\\d{14}-\\d{1,4}$");

        assertThat(orderResponse.orderDetails())
                .as("주문 상세 내역 검증")
                .hasSize(1)
                .first()
                .extracting("brandName", "quantity")
                .containsExactly("adidas", 2L);

        log.info("주문이 정상적으로 생성됨: {}", orderResponse);
    }

    @Test
//    @Transactional(readOnly = true)
// JPA 영속성 컨텍스트를 유지해서 ProductItem 객체를 동일한 것으로 유지하도록 강제 -> savedProductItem과 findByIdWithProduct()로 가져온 객체가 동일한 것으로 인식됨
    @DisplayName("장바구니 주문을 요청했을 때 주문 상세가 장바구니 데이터와 일치하게 생성된다")
    void createOrderDetailTest_FromCart() {
        /// given
        Cart cart = Cart.builder()
                .user(savedUser)
                .quantity(2L) // 장바구니에 2개 담음
                .productItem(savedProductItem)
                .build();
        cartRepository.save(cart);

        Order order = new Order(savedUser, "ORD-202503101234-5678");

        OrderRequestDto orderReq = OrderRequestDto.builder()
                .cartId(cart.getCartId()) // 장바구니 기반 주문
                .itemId(null) // 직접 주문이 아님
                .quantity(null) // 장바구니 수량 사용
                .build();

        /// when
        ProductItem expectedProductItem = productItemRepository.findByIdWithProduct(savedProductItem.getId())
                .orElseThrow(() -> new IllegalArgumentException("상품 아이템이 존재하지 않음"));

        OrderDetail orderDetail = orderService.createOrderDetail(order, orderReq);

        /// then
        assertThat(orderDetail.getProductItem())
                .extracting(ProductItem::getId, ProductItem::getColor, ProductItem::getSize)
                .containsExactly(expectedProductItem.getId(), expectedProductItem.getColor(), expectedProductItem.getSize());

        log.info("장바구니 기반 주문 상세 생성 성공: {}", orderDetail);
    }

    @Test
    @Transactional(readOnly = true) // 트랜잭션 유지로 LazyInitializationException 방지
    @DisplayName("상품페이지 바로 주문을 요청했을 때 주문 상세가 요청에 맞게 생성된다")
    void createOrderDetailTest_DirectOrder() {
        /// given
        Order order = new Order(savedUser, "ORD-202503101234-9999");

        OrderRequestDto orderReq = OrderRequestDto.builder()
                .cartId(null) // 장바구니 주문이 아님
                .itemId(savedProductItem.getId()) // 직접 주문할 상품 ID
                .quantity(2L) // 3개 주문
                .build();

        /// when
        ProductItem expectedProductItem = productItemRepository.findById(savedProductItem.getId())
                .orElseThrow(() -> new IllegalArgumentException("상품 아이템이 존재하지 않음"));

        // 강제 초기화: 트랜잭션 내에서 로딩
        expectedProductItem.getProduct().getItems().size(); // 컬렉션 초기화

        OrderDetail orderDetail = orderService.createOrderDetail(order, orderReq);

        /// then
        assertThat(orderDetail.getProductItem())
                .extracting(ProductItem::getId, ProductItem::getColor, ProductItem::getSize)
                .containsExactly(expectedProductItem.getId(), expectedProductItem.getColor(), expectedProductItem.getSize());

        log.info("바로 주문 상세 생성 성공: {}", orderDetail);
    }

    @Test
    @DisplayName("사용자 정보가 누락된 상태에서 주문을 요청하면 예외가 발생한다")
    void createOrderWithoutUser_ShouldThrowException() {
        /// given
        OrderRequestDto orderReq = OrderRequestDto.builder()
                .cartId(null)
                .itemId(savedProductItem.getId())
                .quantity(2L)
                .build();

        /// when & then
        assertThatThrownBy(() -> orderService.createOrder(null, List.of(orderReq))) // userId가 null
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("주문을 생성하려면 유효한 사용자 ID가 필요합니다.");
    }

    @Test
    @DisplayName("중복된 tossOrderId 생성 시 예외 발생")
    void createOrderWithDuplicateTossOrderId_ShouldThrowException() {
        /// given
        String fixedTossOrderId = "ORD-202503101234-5678";

        OrderRequestDto orderReq = OrderRequestDto.builder()
                .cartId(null)
                .itemId(savedProductItem.getId())
                .quantity(2L)
                .build();

        Order order1 = new Order(savedUser, fixedTossOrderId); // 첫 번째 주문
        orderRepository.save(order1); // 강제로 저장. 중복 유도

        /// when & then
        assertThatThrownBy(() -> {
            Order order2 = new Order(savedUser, fixedTossOrderId); // 두 번째 주문도 같은 ID 사용
            orderRepository.save(order2);
        }).isInstanceOf(Exception.class)
                .hasMessageContaining("could not execute statement");
    }


    @Test
    @DisplayName("주문 요청 시 cartId, itemId, quantity가 모두 null이면 예외가 발생한다")
    void createOrderWithAllNullValues_ShouldThrowException() {
        /// given
        OrderRequestDto orderReq = OrderRequestDto.builder()
                .cartId(null) // 장바구니 ID 없음
                .itemId(null) // 상품 ID 없음
                .quantity(null) // 수량 없음
                .build();

        /// when & then
        assertThatThrownBy(() -> orderService.createOrder(savedUser.getUserId(), List.of(orderReq)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cartId, itemId, quantity 중 하나는 반드시 포함되어야 합니다.");
    }

    @Test
    @DisplayName("[상품 페이지 바로 주문]상품 ID가 null이면 예외가 발생한다")
    void createOrderDetail_WithNullItemId_ShouldThrowException() {
        /// given
        Order order = new Order(savedUser, "ORD-202503101234-5678");

        OrderRequestDto orderReq = OrderRequestDto.builder()
                .cartId(null)  // 장바구니 주문이 아님
                .itemId(null)  // 상품 ID가 null
                .quantity(2L)  // 수량 입력
                .build();

        /// when & then
        assertThatThrownBy(() -> orderService.createOrderDetail(order, orderReq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품 ID가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("[상품 페이지 바로 주문]주문 수량이 0이면 예외가 발생한다")
    void createOrderWithZeroQuantity_ShouldThrowException() {
        /// given
        OrderRequestDto orderReq = OrderRequestDto.builder()
                .cartId(null) // 장바구니 주문이 아님
                .itemId(savedProductItem.getId()) // 직접 주문할 상품 ID
                .quantity(0L) // 잘못된 수량 (0)
                .build();

        /// when & then
        assertThatThrownBy(() -> orderService.createOrder(savedUser.getUserId(), List.of(orderReq)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("주문 수량은 1개 이상이어야 합니다.");
    }

    @Test
    @DisplayName("존재하지 않는 상품으로 주문을 요청하면 예외가 발생한다")
    void createOrderWithInvalidProductId_ShouldThrowException() {
        /// given
        Long invalidProductId = 9999L; // 존재하지 않는 상품 ID

        OrderRequestDto orderReq = OrderRequestDto.builder()
                .cartId(null)
                .itemId(invalidProductId)
                .quantity(2L)
                .build();

        /// when & then
        assertThatThrownBy(() -> orderService.createOrder(savedUser.getUserId(), List.of(orderReq)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품이 존재하지 않습니다. itemId=" + orderReq.itemId());
    }

    @Test
    @DisplayName("주문 상세를 생성할 때 재고가 부족하면 예외가 발생한다")
    void testCreateOrderDetail_StockNotEnough() {
        /// given
        Order order = new Order(savedUser, "ORD-202503101234-8888");

        OrderRequestDto orderReq = OrderRequestDto.builder()
                .cartId(null)
                .itemId(savedProductItem.getId()) // 직접 주문
                .quantity(6L) // 5개 주문 (재고 부족)
                .build();

        /// when & then
        assertThatThrownBy(() -> orderService.createOrderDetail(order, orderReq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("재고가 부족하여 주문할 수 없습니다. itemId=" + savedProductItem.getId());
    }

    @Test
    @DisplayName("같은 사용자가 동일한 주문을 동시에 여러 개 요청할 경우 하나만 성공하고 나머지는 실패해야 한다.")
    void createDuplicateOrder_ShouldAllowOnlyOneAndThrowForOthers() throws InterruptedException {
        /// given
        OrderRequestDto orderReq = OrderRequestDto.builder()
                .cartId(null)
                .itemId(savedProductItem.getId()) // 동일 상품
                .quantity(2L)
                .build();

        int threadCount = 5; // 5개 스레드로 동시 주문 테스트
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        /// when : 여러 개의 주문을 동시에 실행
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    orderService.createOrder(savedUser.getUserId(), List.of(orderReq));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("주문 실패: {}", e.getMessage());
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드 실행 완료 대기
        executorService.shutdown();

        /// then (하나만 성공해야 함)
        long orderCount = orderRepository.countByUserAndProductItem(savedUser, savedProductItem.getId());

        // ❓비교 #1
        assertThat(successCount.get()).isEqualTo(1); // 단 하나의 주문만 성공
        assertThat(failureCount.get()).isEqualTo(threadCount - 1); // 나머지는 실패해야 함
        assertThat(orderCount).isEqualTo(1); // 데이터베이스에도 단 하나만 저장되어야 함

        // ❓비교 #2
        assertThat(successCount.get())
                .as("단 하나의 주문만 성공해야 한다")
                .isEqualTo(1);

        assertThat(failureCount.get())
                .as("나머지 스레드는 실패해야 한다")
                .isEqualTo(threadCount - 1);

        assertThat(orderRepository.countByUserAndProductItem(savedUser, savedProductItem.getId()))
                .as("데이터베이스에도 단 하나의 주문만 존재해야 한다")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("같은 사용자가 동시에 여러 개의 주문을 요청할 경우 진행 중인 주문이 있다면 차단되어야 한다.")
    void createMultipleOrdersSimultaneously_ShouldThrowException() {
        /// given
        OrderRequestDto orderReq1 = OrderRequestDto.builder()
                .cartId(null)
                .itemId(savedProductItem.getId())
                .quantity(2L)
                .build();

        OrderRequestDto orderReq2 = OrderRequestDto.builder()
                .cartId(null)
                .itemId(savedProductItem.getId())
                .quantity(3L)
                .build();

        // 첫 번째 주문 정상 진행
        orderService.createOrder(savedUser.getUserId(), List.of(orderReq1));

        /// when & then
        assertThatThrownBy(() -> orderService.createOrder(savedUser.getUserId(), List.of(orderReq2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("현재 진행 중인 주문이 있습니다.");
    }
}
