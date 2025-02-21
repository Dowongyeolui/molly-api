package org.example.mollyapi.delivery.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.delivery.entity.Delivery;
import org.example.mollyapi.delivery.repository.DeliveryRepository;
import org.example.mollyapi.delivery.type.DeliveryStatus;
import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.order.repository.OrderRepository;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional
    public void updateDeliveryStatus(Long orderId, DeliveryStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문 정보를 찾을 수 없습니다. orderId=" + orderId));

        Delivery delivery = order.getDelivery();
        if (delivery == null) {
            throw new IllegalStateException("해당 주문에 연결된 배송 정보가 없습니다. orderId=" + orderId);
        }

        log.info("배송 상태 변경: orderId={}, deliveryId={}, {} → {}", orderId, delivery.getId(), delivery.getStatus(), status);
        delivery.setStatus(status);
        deliveryRepository.save(delivery);

        // 배송이 ARRIVED 상태로 변경되면 포인트 적립
        if (status == DeliveryStatus.ARRIVED) {
            applyPointReward(order);
        }
    }

    @Transactional
    public void applyPointReward(Order order) {
        User user = order.getUser();

        // 포인트 적립된 경우 중복 적립 방지
        if (order.getPointSave() != null && order.getPointSave() > 0) {
            log.warn("이미 적립된 포인트가 존재하여 적립을 중단합니다. orderId={}, 기존 적립 포인트={}", order.getId(), order.getPointSave());
            return;
        }

        // 주문 금액의 10% 포인트 적립 (소수점 버림)
        int rewardPoint = (int) (order.getPaymentAmount() * 0.1);
        log.info("결제 금액: {}, 적립 포인트: {}", order.getPaymentAmount(), rewardPoint);

        user.updatePoint(rewardPoint);
        userRepository.save(user);

        // Order.point_save 적립 포인트 저장
        order.setPointSave(rewardPoint);
        orderRepository.save(order);

        log.info("포인트 적립 완료: User ID = {}, 적립 포인트 = {}", user.getUserId(), rewardPoint);
    }
}