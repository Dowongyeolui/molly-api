package org.example.mollyapi.order.controller;

import org.example.mollyapi.delivery.entity.Delivery;
import org.example.mollyapi.delivery.repository.DeliveryRepository;
import org.example.mollyapi.delivery.type.DeliveryStatus;
import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.order.repository.OrderRepository;
import org.example.mollyapi.order.service.OrderServiceImpl;
import org.example.mollyapi.order.type.CancelStatus;
import org.example.mollyapi.order.type.OrderStatus;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class OrderWithdrawControllerTest {

    @Autowired
    private OrderServiceImpl orderServiceImpl;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void 배송_전_주문_철회_테스트() {
        // given - 주문과 배송 정보를 DB에서 가져옴
        Order order56 = orderRepository.findById(56L)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. orderId=56"));
        Delivery deliveryReady = order56.getDelivery();
        User user = userRepository.findById(order56.getUser().getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 주문 철회 전 포인트 값 조회
        int previousUserPoint = user.getPoint();
        Long expectedRefundedPoint = order56.getPointUsage() + order56.getPaymentAmount();

        // when - 주문 철회 실행
        orderServiceImpl.withdrawOrder(56L);

        // then - 주문 철회가 정상적으로 이루어졌는지 검증
        assertThat(order56.getCancelStatus()).isEqualTo(CancelStatus.COMPLETED);
        assertThat(order56.getStatus()).isEqualTo(OrderStatus.WITHDRAW);
        assertThat(deliveryReady.getStatus()).isEqualTo(DeliveryStatus.CANCELED);

        // 포인트가 환불되었는지 검증
        user = userRepository.findById(order56.getUser().getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        assertThat(user.getPoint()).isEqualTo(previousUserPoint + expectedRefundedPoint);
    }

    @Test
    void 배송_후_주문_철회_테스트() {
        // given - 주문과 배송 정보를 DB에서 가져옴
        Order order57 = orderRepository.findById(57L)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. orderId=57"));
        Delivery deliveryArrived = order57.getDelivery();
        User user = userRepository.findById(order57.getUser().getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 기존 포인트 값 저장
        int previousUserPoint = user.getPoint();

        // Null 체크 후 기본값 처리
        int pointUsage = (order57.getPointUsage() != null) ? order57.getPointUsage() : 0;
        int pointSave = (order57.getPointSave() != null) ? order57.getPointSave() : 0;
        Long paymentAmount = (order57.getPaymentAmount() != null) ? order57.getPaymentAmount() : 0;

        // 포인트 환불 예상 금액 계산
        Long expectedRefundedPoint = pointUsage + paymentAmount - pointSave;

        // when - 주문 철회 실행
        orderServiceImpl.withdrawOrder(57L);

        // then - 주문 철회가 정상적으로 이루어졌는지 검증
        assertThat(order57.getCancelStatus()).isEqualTo(CancelStatus.COMPLETED);
        assertThat(order57.getStatus()).isEqualTo(OrderStatus.WITHDRAW);
        assertThat(deliveryArrived.getStatus()).isEqualTo(DeliveryStatus.RETURNED);

        // 포인트가 환불되었는지 검증
        user = userRepository.findById(order57.getUser().getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        assertThat(user.getPoint()).isEqualTo(previousUserPoint + expectedRefundedPoint);
    }
}