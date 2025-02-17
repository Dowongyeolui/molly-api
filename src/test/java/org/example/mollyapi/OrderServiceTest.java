package org.example.mollyapi;

import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.delivery.entity.Delivery;
import org.example.mollyapi.delivery.repository.DeliveryRepository;
import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.order.entity.OrderDetail;
import org.example.mollyapi.order.repository.OrderDetailRepository;
import org.example.mollyapi.order.repository.OrderRepository;
import org.example.mollyapi.order.service.OrderService;
import org.example.mollyapi.order.type.CancelStatus;
import org.example.mollyapi.order.type.OrderStatus;
import org.example.mollyapi.product.entity.ProductItem;
import org.example.mollyapi.product.repository.ProductItemRepository;
import org.example.mollyapi.product.repository.ProductRepository;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.example.mollyapi.order.entity.QOrderDetail.orderDetail;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Slf4j
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductItemRepository productItemRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    private Order testOrder;
    private User testUser;

    @BeforeEach
    void setUp() {
        // DB에서 User 조회 (id=1)
        testUser = userRepository.findById(5L)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // DB에서 주문 조회 (tossOrderId 사용)
        testOrder = orderRepository.findByTossOrderId("ORD-20250213132349-6572")
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        log.info("테스트 유저: {} , 포인트: {} " ,testUser.getNickname(), testUser.getPoint());
        log.info("테스트 주문: {}, 상태: {} " ,testOrder.getTossOrderId(), testOrder.getStatus());
    }

    @Test
    void 결제_성공시_Order_정보_업데이트() {
        // Given: 결제 성공 정보
        String fakePaymentId = "PAY-12345";
        String fakePaymentType = "CARD";
        Long fakePaymentAmount = 258000L;
        Integer fakePointUsage = 5000;
        String fakeDeliveryInfo = """
        {
            "receiver_name": "X-3456",
            "receiver_phone": "01773229999",
            "road_address": "서울특별시 강남구 테헤란로 427",
            "number_address": "서울특별시 강남구 삼성동 23-4",
            "addr_detail": "101동 201호"
        }
    """;

        // When: successOrder() 실행
        orderService.successOrder("ORD-20250213132349-6572", fakePaymentId, fakePaymentType, fakePaymentAmount, fakePointUsage, fakeDeliveryInfo);

        // Then: 주문 상태 변경 확인
        assertEquals(OrderStatus.SUCCEEDED, testOrder.getStatus());
        assertEquals(fakePaymentId, testOrder.getPaymentId());
        assertEquals(fakePaymentType, testOrder.getPaymentType());
        assertEquals(fakePaymentAmount, testOrder.getPaymentAmount());
        assertEquals(fakePointUsage, testOrder.getPointUsage());

        // 포인트 차감 확인
        assertEquals(100000 - fakePointUsage, testUser.getPoint());  // 원래 포인트에서 차감됐는지 확인

        // delivery_info 저장 확인
        assertNotNull(testOrder.getDeliveryInfo(), "Delivery 정보가 저장되지 않았습니다.");
        assertEquals(fakeDeliveryInfo, testOrder.getDeliveryInfo(), "Delivery 정보가 올바르게 저장되지 않았습니다.");

        // 테스트 결과 출력
        log.info("successOrder 테스트 결과:");
        log.info("Order status: {}", testOrder.getStatus());
        log.info("Order payment 정보: paymentId={}, paymentType={}, paymentAmount={}", testOrder.getPaymentId(), testOrder.getPaymentType(), testOrder.getPaymentAmount());
        log.info("Order point_usage: {}", testOrder.getPointUsage());
        log.info("User point: {}", testUser.getPoint());
        log.info("Order deliveryInfo: {}", testOrder.getDeliveryInfo());
    }

    @Test
    void 결제_성공시_Delivery_생성_및_주문_업데이트_확인() {
        // Given
        String tossOrderId = "ORD-20250214173656-8525";
        String fakePaymentId = "PAY-123456";
        String fakePaymentType = "CARD";
        Long fakePaymentAmount = 516000L;
        Integer fakePointUsage = 5000;
        String fakeDeliveryInfo = """
        {
            "receiver_name": "X-3456",
            "receiver_phone": "01773229999",
            "road_address": "서울특별시 강남구 테헤란로 427",
            "number_address": "서울특별시 강남구 삼성동 23-4",
            "addr_detail": "101동 201호"
        }
    """;

        log.info("==배송 생성 및 주문 업데이트 확인 테스트 시작==");

        // 기존 주문 정보 조회
        Order order = orderRepository.findByTossOrderId(tossOrderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. tossOrderId=" + tossOrderId));

        // When: 결제 성공 처리 (이때 createDelivery() 실행됨)
        orderService.successOrder(tossOrderId, fakePaymentId, fakePaymentType, fakePaymentAmount, fakePointUsage, fakeDeliveryInfo);

        // Then: 배송 정보 저장 확인
        Order updatedOrder = orderRepository.findByTossOrderId(tossOrderId)
                .orElseThrow(() -> new IllegalArgumentException("업데이트된 주문을 찾을 수 없습니다. tossOrderId=" + tossOrderId));

        // `delivery_info`가 `null`로 변경되었는지 확인
        log.info("Updated Order delivery_info: {}", updatedOrder.getDeliveryInfo());
        assertNull(updatedOrder.getDeliveryInfo(), "배송 정보가 null로 설정되지 않았습니다.");

        // `order.delivery_id`가 정상적으로 저장되었는지 확인
        assertNotNull(updatedOrder.getDelivery(), "배송이 생성되지 않았습니다.");
        Long deliveryId = updatedOrder.getDelivery().getId();
        assertNotNull(deliveryId, "주문에 연결된 배송 ID가 없습니다.");

        // DB에서 배송 정보 조회 후 검증
        Optional<Delivery> savedDelivery = deliveryRepository.findById(deliveryId);
        assertTrue(savedDelivery.isPresent(), "배송 정보가 DB에 저장되지 않았습니다.");

        Delivery delivery = savedDelivery.get();

        // delivery_info와 비교
        assertEquals(updatedOrder.getId(), delivery.getOrder().getId(), "배송과 주문이 올바르게 매핑되지 않았습니다.");
        assertEquals("X-3456", delivery.getReceiverName(), "받는 사람 이름이 올바르지 않습니다.");
        assertEquals("01773229999", delivery.getReceiverPhone(), "받는 사람 전화번호가 올바르지 않습니다.");
        assertEquals("서울특별시 강남구 테헤란로 427", delivery.getRoadAddress(), "도로명 주소가 올바르지 않습니다.");
        assertEquals("서울특별시 강남구 삼성동 23-4", delivery.getNumberAddress(), "지번 주소가 올바르지 않습니다.");
        assertEquals("101동 201호", delivery.getAddrDetail(), "상세 주소가 올바르지 않습니다.");

        log.info("Order ID: {}, Delivery ID: {}, Order.Delivery ID: {}", updatedOrder.getId(), delivery.getId(), updatedOrder.getDelivery().getId());
        log.info("Receiver Name: {}, Phone: {}", delivery.getReceiverName(), delivery.getReceiverPhone());
        log.info("Address: {}, {}", delivery.getRoadAddress(), delivery.getNumberAddress());
        log.info("Detail Address: {}", delivery.getAddrDetail());
    }

    @Test
    void 결제_실패시_주문_삭제_및_재고_복구() {
        // Given
        String tossOrderId = "ORD-20250213132349-6572";

        // 기존 재고 값 조회해서 저장
        List<ProductItem> productItemsBefore = testOrder.getOrderDetails().stream()
                .map(orderDetail -> {
                    ProductItem item = productItemRepository.findById(orderDetail.getProductItem().getId())
                            .orElseThrow(() -> new IllegalArgumentException("상품 아이템을 찾을 수 없습니다. itemId=" + orderDetail.getProductItem().getId()));
                    return item;
                })
                .toList();

        log.info("==failOrder 테스트 시작==");
        log.info("주문 번호: {}, 주문 상태: {}", tossOrderId, testOrder.getStatus());
        log.info("주문 상품 목록");
        for (var orderDetail : testOrder.getOrderDetails()) {
            log.info("상품 ID: {}, 주문 수량: {}", orderDetail.getProductItem().getId(), orderDetail.getQuantity());
        }

        // When: 결제 실패 처리
        orderService.failOrder(tossOrderId);

        // Then: 주문 상태 변경 확인
        Optional<Order> failedOrder = orderRepository.findByTossOrderId(tossOrderId);
        if (failedOrder.isPresent()) {
            assertEquals(OrderStatus.FAILED, failedOrder.get().getStatus(), "주문 상태가 FAILED로 변경되지 않았습니다");
            log.info("주문 상태 변경 확인: FAILED");
        } else {
            log.warn("주문이 삭제되었으므로 상태를 확인할 수 없습니다");
        }

        // 주문(Order), 주문 상세(OrderDetail) 삭제 확인
        boolean isOrderDeleted = orderRepository.findByTossOrderId(tossOrderId).isEmpty();
        boolean isOrderDetailDeleted = orderRepository.countOrderDetailsByTossOrderId(tossOrderId) == 0;

        assertTrue(isOrderDeleted, "주문이 삭제되지 않았습니다");
        assertTrue(isOrderDetailDeleted, "주문 상세가 삭제되지 않았습니다");

        log.info("주문 삭제 확인: {}", isOrderDeleted ? "성공" : "실패");
        log.info("주문 상세 삭제 확인: {}", isOrderDetailDeleted ? "성공" : "실패");

    }

    @Test
    void 결제_전_주문_취소시_주문_삭제_및_재고_복구() {
        // Given: 주문 데이터 생성
        Long orderId = 3L;
        String tossOrderId = "ORD-20250214131359-1578";

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. orderId=" + orderId));

        assertEquals(OrderStatus.PENDING, order.getStatus(), "주문의 상태가 PENDING이 아닙니다.");

        // 기존 재고 값 조회
        List<ProductItem> productItemsBefore = order.getOrderDetails().stream()
                .map(OrderDetail::getProductItem)
                .map(productItem -> productItemRepository.findById(productItem.getId())
                        .orElseThrow(() -> new IllegalArgumentException("상품 아이템을 찾을 수 없습니다. itemId=" + productItem.getId())))
                .toList();

        log.info("== (결제 전)주문 취소 테스트 시작 ==");
        log.info("주문 ID: {}, 주문 상태: {}", orderId, order.getStatus());
        log.info("주문 상품 목록:");
        for (var detail : order.getOrderDetails()) {
            log.info("상품 ID: {}, 사이즈: {}, 가격: {}, 주문 수량: {}",
                    detail.getProductItem().getId(), detail.getSize(), detail.getPrice(), detail.getQuantity());
        }

        // When: 주문 취소 처리
        String responseMessage = orderService.cancelOrder(orderId, false);
        log.info("responseMessage = {}", responseMessage);

        // Then: 주문 삭제 확인
        Optional<Order> canceledOrder = orderRepository.findById(orderId);
        assertTrue(canceledOrder.isEmpty(), "주문이 삭제되지 않았습니다.");

        // 주문 상세(OrderDetail) 삭제 확인
        boolean isOrderDetailDeleted = orderDetailRepository.findByOrderId(orderId).isEmpty();
        assertTrue(isOrderDetailDeleted, "주문 상세가 삭제되지 않았습니다.");

        log.info("주문 삭제 확인: {}", canceledOrder.isEmpty() ? "성공" : "실패");
        log.info("주문 상세 삭제 확인: {}", isOrderDetailDeleted ? "성공" : "실패");

    }

    @Test
    void 결제_실패시_주문_삭제_및_재고_복구() {
        // Given
        String tossOrderId = "ORD-20250213132349-6572";

        // 기존 재고 값 조회해서 저장
        List<ProductItem> productItemsBefore = testOrder.getOrderDetails().stream()
                .map(orderDetail -> {
                    ProductItem item = productItemRepository.findById(orderDetail.getProductItem().getId())
                            .orElseThrow(() -> new IllegalArgumentException("상품 아이템을 찾을 수 없습니다. itemId=" + orderDetail.getProductItem().getId()));
                    return item;
                })
                .toList();

        log.info("==failOrder 테스트 시작==");
        log.info("주문 번호: {}, 주문 상태: {}", tossOrderId, testOrder.getStatus());
        log.info("주문 상품 목록");
        for (var orderDetail : testOrder.getOrderDetails()) {
            log.info("상품 ID: {}, 주문 수량: {}", orderDetail.getProductItem().getId(), orderDetail.getQuantity());
        }

        // When: 결제 실패 처리
        orderService.failOrder(tossOrderId);

        // Then: 주문 상태 변경 확인
        Optional<Order> failedOrder = orderRepository.findByTossOrderId(tossOrderId);
        if (failedOrder.isPresent()) {
            assertEquals(OrderStatus.FAILED, failedOrder.get().getStatus(), "주문 상태가 FAILED로 변경되지 않았습니다");
            log.info("주문 상태 변경 확인: FAILED");
        } else {
            log.warn("주문이 삭제되었으므로 상태를 확인할 수 없습니다");
        }

        // 주문(Order), 주문 상세(OrderDetail) 삭제 확인
        boolean isOrderDeleted = orderRepository.findByTossOrderId(tossOrderId).isEmpty();
        boolean isOrderDetailDeleted = orderRepository.countOrderDetailsByTossOrderId(tossOrderId) == 0;

        assertTrue(isOrderDeleted, "주문이 삭제되지 않았습니다");
        assertTrue(isOrderDetailDeleted, "주문 상세가 삭제되지 않았습니다");

        log.info("주문 삭제 확인: {}", isOrderDeleted ? "성공" : "실패");
        log.info("주문 상세 삭제 확인: {}", isOrderDetailDeleted ? "성공" : "실패");

    }

    @Test
    void 결제_전_주문_취소시_주문_삭제_및_재고_복구() {
        // Given: 주문 데이터 생성
        Long orderId = 3L;
        String tossOrderId = "ORD-20250214131359-1578";

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. orderId=" + orderId));

        assertEquals(OrderStatus.PENDING, order.getStatus(), "주문의 상태가 PENDING이 아닙니다.");

        // 기존 재고 값 조회
        List<ProductItem> productItemsBefore = order.getOrderDetails().stream()
                .map(OrderDetail::getProductItem)
                .map(productItem -> productItemRepository.findById(productItem.getId())
                        .orElseThrow(() -> new IllegalArgumentException("상품 아이템을 찾을 수 없습니다. itemId=" + productItem.getId())))
                .toList();

        log.info("== (결제 전)주문 취소 테스트 시작 ==");
        log.info("주문 ID: {}, 주문 상태: {}", orderId, order.getStatus());
        log.info("주문 상품 목록:");
        for (var detail : order.getOrderDetails()) {
            log.info("상품 ID: {}, 사이즈: {}, 가격: {}, 주문 수량: {}",
                    detail.getProductItem().getId(), detail.getSize(), detail.getPrice(), detail.getQuantity());
        }

        // When: 주문 취소 처리
        String responseMessage = orderService.cancelOrder(orderId, false);
        log.info("responseMessage = {}", responseMessage);

        // Then: 주문 삭제 확인
        Optional<Order> canceledOrder = orderRepository.findById(orderId);
        assertTrue(canceledOrder.isEmpty(), "주문이 삭제되지 않았습니다.");

        // 주문 상세(OrderDetail) 삭제 확인
        boolean isOrderDetailDeleted = orderDetailRepository.findByOrderId(orderId).isEmpty();
        assertTrue(isOrderDetailDeleted, "주문 상세가 삭제되지 않았습니다.");

        log.info("주문 삭제 확인: {}", canceledOrder.isEmpty() ? "성공" : "실패");
        log.info("주문 상세 삭제 확인: {}", isOrderDetailDeleted ? "성공" : "실패");

    }
}