package org.example.mollyapi.order.service;

import org.example.mollyapi.order.dto.OrderHistoryResponseDto;
import org.example.mollyapi.order.dto.OrderRequestDto;
import org.example.mollyapi.order.dto.OrderResponseDto;
import org.example.mollyapi.payment.dto.response.PaymentResDto;
import org.example.mollyapi.delivery.dto.DeliveryReqDto;

import java.util.List;

public interface OrderService {
    OrderHistoryResponseDto getUserOrders(Long userId);
    OrderResponseDto getOrderDetails(Long orderId);
    OrderResponseDto createOrder(Long userId, List<OrderRequestDto> orderRequests);
    String cancelOrder(Long orderId);
    void expireOrder(Long orderId);
    PaymentResDto processPayment(Long userId, String paymentKey, String tossOrderId, Long amount, String point, String paymentType, DeliveryReqDto deliveryInfo);
    void withdrawOrder(Long orderId);
}