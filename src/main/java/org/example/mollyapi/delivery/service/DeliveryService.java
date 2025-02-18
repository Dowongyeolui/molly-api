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
    public void updateDeliveryStatus(Long deliveryId, DeliveryStatus status) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 배송 정보를 찾을 수 없습니다. deliveryId=" + deliveryId));

        log.info("배송 상태 변경: Delivery Id = {}, status = {}", delivery.getId(), status);
        delivery.setStatus(status);
        deliveryRepository.save(delivery);

        // 배송이 ARRIVED 상태로 변경되면 포인트 적립
        if (status == DeliveryStatus.ARRIVED) {
            applyPointReward(delivery);
        }
    }

    @Transactional
    public void applyPointReward(Delivery delivery) {
        Order order = delivery.getOrder();
        User user = order.getUser();

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