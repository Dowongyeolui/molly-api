package org.example.mollyapi.delivery;

import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.delivery.entity.Delivery;
import org.example.mollyapi.delivery.repository.DeliveryRepository;
import org.example.mollyapi.delivery.service.DeliveryService;
import org.example.mollyapi.delivery.type.DeliveryStatus;
import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.order.repository.OrderRepository;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Slf4j
class DeliveryServiceTest {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private Delivery testDelivery;
    private Order testOrder;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트용 주문 조회 (order_id = 6)
        testOrder = orderRepository.findById(6L)
                .orElseThrow(() -> new IllegalArgumentException("테스트 주문을 찾을 수 없습니다. order_id=6"));

        // 주문에 연결된 유저 조회
        testUser = testOrder.getUser();
        assertNotNull(testUser, "주문에 연결된 유저가 없습니다.");

        // 주문에 연결된 배송 조회 (delivery_id = 1)
        testDelivery = deliveryRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("테스트 배송을 찾을 수 없습니다. delivery_id=1"));

        log.info("테스트 데이터 - 주문 ID: {}, 배송 ID: {}, 유저 ID: {}", testOrder.getId(), testDelivery.getId(), testUser.getUserId());
    }

    @Test
    void 배송_상태_변경_및_포인트_적립_테스트() {
        // Given: Delivery.status=READY(default)
        assertEquals(DeliveryStatus.READY, testDelivery.getStatus(), "초기 배송 상태가 READY가 아닙니다.");

        // When: Delivery.status=SHIPPING으로 변경
        deliveryService.updateDeliveryStatus(testDelivery.getId(), DeliveryStatus.SHIPPING);
        Delivery updatedDelivery1 = deliveryRepository.findById(testDelivery.getId()).orElseThrow();
        assertEquals(DeliveryStatus.SHIPPING, updatedDelivery1.getStatus(), "배송 상태가 SHIPPING으로 변경되지 않았습니다.");

        // When: Delivery.status=ARRIVED로 변경 (<<포인트 적립)
        int beforePoint = testUser.getPoint(); // 기존 유저 포인트
        deliveryService.updateDeliveryStatus(testDelivery.getId(), DeliveryStatus.ARRIVED);

        // Then: Delivery.status 확인
        Delivery updatedDelivery2 = deliveryRepository.findById(testDelivery.getId()).orElseThrow();
        assertEquals(DeliveryStatus.ARRIVED, updatedDelivery2.getStatus(), "배송 상태가 ARRIVED로 변경되지 않았습니다.");

        // Then: Order.point 업데이트 확인
        int expectedPoint = beforePoint + (int) (testOrder.getTotalAmount() * 0.1);
        User updatedUser = userRepository.findById(testUser.getUserId()).orElseThrow();
        assertEquals(expectedPoint, updatedUser.getPoint(), "포인트 적립이 정상적으로 되지 않았습니다.");

        // Then: Order.pointSave 업데이트 확인
        Order updatedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
        int expectedOrderPointSave = (int) (updatedOrder.getPaymentAmount() * 0.1);
        assertEquals(expectedOrderPointSave, updatedOrder.getPointSave(), "Order.pointSave 값이 정상적으로 저장되지 않았습니다.");

        log.info("==배송 상태 변경 및 포인트 적립 테스트 완료==");
        log.info("최종 배송 상태: {}", updatedDelivery2.getStatus());
        log.info("포인트 적립 확인: 기존 포인트 = {}, 적립 후 포인트 = {}", beforePoint, updatedUser.getPoint());
        log.info("Order.pointSave 확인: 예상 = {}, 실제 = {}", expectedOrderPointSave, updatedOrder.getPointSave());
    }
}