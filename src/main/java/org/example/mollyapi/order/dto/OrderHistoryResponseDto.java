package org.example.mollyapi.order.dto;

import lombok.Getter;
import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.order.type.OrderStatus;
import org.example.mollyapi.payment.entity.Payment;
import org.example.mollyapi.payment.repository.PaymentRepository;
import org.example.mollyapi.review.repository.ReviewRepository;
import org.springframework.data.domain.PageRequest;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class OrderHistoryResponseDto {
    private Long userId;
    private List<OrderSummaryDto> orders;

    public OrderHistoryResponseDto(Long userId, List<Order> orders, PaymentRepository paymentRepository, ReviewRepository reviewRepository) {
        this.userId = userId;
        this.orders = orders.stream()
                .map(order -> OrderSummaryDto.from(userId, order, paymentRepository, reviewRepository))
                .collect(Collectors.toList());
    }

    @Getter
    public static class OrderSummaryDto {
        private String tossOrderId;
        private OrderStatus orderStatus;
        private String orderedAt;
        private Long paymentAmount;
        private String deliveryStatus;
        private final List<OrderDetailWithReviewResponseDto> orderDetails;

        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        public static OrderSummaryDto from(Long userId, Order order, PaymentRepository paymentRepository, ReviewRepository reviewRepository) {
            List<Payment> payments = paymentRepository.findLatestPaymentByOrderId(order.getId(), PageRequest.of(0, 1));
            Long paymentAmount = payments.isEmpty() ? 0L : payments.get(0).getAmount();
            return new OrderSummaryDto(userId, order, paymentAmount, reviewRepository);
        }

        private OrderSummaryDto(Long userId, Order order, Long paymentAmount, ReviewRepository reviewRepository) {
            this.tossOrderId = order.getTossOrderId();
            this.orderStatus = order.getStatus();
            this.orderedAt = order.getOrderedAt() != null ? order.getOrderedAt().format(FORMATTER) : null;
            this.paymentAmount = paymentAmount;
            this.deliveryStatus = order.getDelivery() != null ? order.getDelivery().getStatus().name() : null;
            this.orderDetails = order.getOrderDetails().stream()
                    .map(orderDetail -> OrderDetailWithReviewResponseDto.from(userId, orderDetail, reviewRepository))
                    .collect(Collectors.toList());
        }
    }
}