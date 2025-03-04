package org.example.mollyapi.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.order.repository.OrderDetailRepository;
import org.example.mollyapi.order.repository.OrderRepository;
import org.example.mollyapi.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSchedulerService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public int deleteExpiredOrders() {
        List<Order> expiredOrders = orderRepository.findExpiredPendingOrders(LocalDateTime.now());

        if (expiredOrders.isEmpty()) {
            log.info("삭제할 만료된 주문 없음");
            return 0;
        }

        List<Long> orderIds = expiredOrders.stream().map(Order::getId).toList();
        log.info("삭제할 주문 ID 목록: {}", orderIds);

        // 삭제할 OrderDetail ID 가져오기
        List<Long> orderDetailIds = orderDetailRepository.findOrderDetailIdsByOrderIds(orderIds);

        if (!orderDetailIds.isEmpty()) {
            // OrderDetail에 연결된 Review 삭제
            int deletedReviews = reviewRepository.deleteByOrderDetailIds(orderDetailIds);
            log.info("삭제된 리뷰 개수: {}", deletedReviews);

            // OrderDetail 삭제
            int deletedOrderDetails = orderDetailRepository.deleteAllByOrderIds(orderIds);
            log.info("삭제된 주문 상세 개수: {}", deletedOrderDetails);
        }

        // Order 삭제
        orderRepository.deleteAll(expiredOrders);
        log.info("삭제된 주문 개수: {} (삭제된 주문 ID 목록: {})", orderIds.size(), orderIds);

        return orderIds.size();
    }
}