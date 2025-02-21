package org.example.mollyapi.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.address.dto.AddressResponseDto;
import org.example.mollyapi.address.repository.AddressRepository;
import org.example.mollyapi.delivery.entity.Delivery;
import org.example.mollyapi.delivery.repository.DeliveryRepository;
import org.example.mollyapi.delivery.type.DeliveryStatus;
import org.example.mollyapi.order.dto.OrderHistoryResponseDto;
import org.example.mollyapi.order.dto.OrderRequestDto;
import org.example.mollyapi.order.dto.OrderResponseDto;
import org.example.mollyapi.order.entity.*;
import org.example.mollyapi.order.repository.OrderRepository;
import org.example.mollyapi.order.repository.OrderDetailRepository;
import org.example.mollyapi.order.type.CancelStatus;
import org.example.mollyapi.order.type.OrderStatus;
import org.example.mollyapi.payment.repository.PaymentRepository;
import org.example.mollyapi.product.entity.Product;
import org.example.mollyapi.product.entity.ProductItem;
import org.example.mollyapi.product.repository.ProductItemRepository;
import org.example.mollyapi.product.repository.ProductRepository;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final ProductItemRepository productItemRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final DeliveryRepository deliveryRepository;
    private final AddressRepository addressRepository;


    // 사용자의 주문 내역 조회 (GET /orders/{userId})
    public OrderHistoryResponseDto getUserOrders(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));

        List<Order> orders = orderRepository.findOrdersByUserAndStatusIn(
                user, List.of(OrderStatus.SUCCEEDED, OrderStatus.WITHDRAW)
        );

        return new OrderHistoryResponseDto(userId, orders, paymentRepository);
    }


    // 주문 상세 조회 (GET /orders/{orderId})
    public OrderResponseDto getOrderDetails(Long orderId) {
        // 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다. orderId=" + orderId));

        User user = order.getUser();

        // 사용자 보유 포인트 조회
        Integer userPoint = user.getPoint();

        // 기본 배송지 조회
        AddressResponseDto defaultAddress = addressRepository.findByUserAndDefaultAddr(user, true)
                .map(AddressResponseDto::from)
                .orElse(null);

        // 주문 상세 응답 반환
        return OrderResponseDto.from(order, paymentRepository, userPoint, defaultAddress);
    }


    // 주문 생성
    public OrderResponseDto createOrder(Long userId, List<OrderRequestDto> orderRequests) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));

        // 사용자 보유 포인트 가져오기
        Integer userPoint = user.getPoint();

        // 기본 배송지 찾기
        AddressResponseDto defaultAddress = addressRepository.findByUserAndDefaultAddr(user, true)
                .map(AddressResponseDto::from)
                .orElse(null);

        // 결제용 주문 ID 생성
        String tossOrderId = generateTossOrderId();

        // 새로운 주문 생성 (초기 상태는 PENDING)
        Order order = Order.builder()
                .user(user)
                .tossOrderId(tossOrderId)
                .totalAmount(0L)
                .status(OrderStatus.PENDING)
                .cancelStatus(CancelStatus.NONE)
                .expirationTime(LocalDateTime.now().plusMinutes(10))
                .build();
        orderRepository.save(order);

        List<OrderDetail> orderDetails = orderRequests.stream().map(req -> {
            // 비관적 락을 걸어 Product 조회
            Product product = productRepository.findWithLockById(req.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. productId=" + req.getProductId()));

            // ProductItem을 Repository에서 직접 조회
            ProductItem productItem = productItemRepository.findById(req.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("상품 아이템을 찾을 수 없습니다. itemId=" + req.getItemId()));

            // 재고 체크
            if (productItem.getQuantity() < req.getQuantity()) {
                order.markAsFailed();  // 주문 실패 처리
                throw new IllegalArgumentException("재고가 부족하여 주문이 불가능합니다. itemId=" + req.getItemId());
            }

            // 재고 감소
            productItem.decreaseStock(Math.toIntExact(req.getQuantity()));
            productItemRepository.save(productItem);  // 감소한 재고 저장

            return OrderDetail.builder()
                    .order(order)
                    .productItem(productItem)
                    .size(productItem.getSize())
                    .price(product.getPrice())
                    .quantity(req.getQuantity())
                    .brandName(product.getBrandName())
                    .productName(product.getProductName())
                    .cartId(req.getCartId())
                    .build();
        }).collect(Collectors.toList());

        // 주문 상세(OrderDetail) 저장
        orderDetailRepository.saveAll(orderDetails);

        // 총 결제 금액 계산 후 Order에 반영
        long totalAmount = orderDetails.stream()
                .mapToLong(d -> d.getPrice() * d.getQuantity())
                .sum();
        order.setTotalAmount(totalAmount);

        orderRepository.save(order);

        return new OrderResponseDto(order, orderDetails, paymentRepository, userPoint, defaultAddress);
    }

    private String generateTossOrderId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomNum = new Random().nextInt(9000) + 1000;
        return "ORD-" + timestamp + "-" + randomNum;
    }

    // 주문 취소: 결제 요청 전 주문을 취소하는 경우
    @Transactional
    public String cancelOrder(Long orderId, boolean isExpired) {
        // 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다. orderId=" + orderId));

        // 주문 상태가 PENDING이 아닐 경우 취소 불가
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("결제 요청이 진행된 주문은 취소할 수 없습니다.");
        }

        // 주문 상태를 CANCELED로 변경
        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);

        // 주문에 속한 OrderDetail 조회 및 재고 복구
        List<OrderDetail> orderDetails = order.getOrderDetails();
        for (OrderDetail detail : orderDetails) {
            ProductItem productItem = detail.getProductItem();
            if (productItem != null) {
                log.info("[Before] 재고 복구 전 - 상품 ID: {}, 기존 재고: {}, 주문 수량: {}",
                        productItem.getId(), productItem.getQuantity(), detail.getQuantity());

                productItem.restoreStock(detail.getQuantity()); // 재고 복구
                productItemRepository.save(productItem);

                log.info("[After] 재고 복구 완료 - 상품 ID: {}, 실행 후 재고: {}",
                        productItem.getId(), productItem.getQuantity());
            } else {
                log.warn("ProductItem이 null입니다. OrderDetail ID: {}", detail.getId());
            }
        }

        // 주문 및 주문 상세 삭제 (Cascade로 OrderDetail도 삭제됨)
        orderRepository.delete(order);

        // 클라이언트에 응답 메시지 반환
        return isExpired ? "요청한 시간이 초과되어 주문이 취소되었습니다." : "주문을 취소했습니다.";
    }

    // 재고 복구
    private void restoreStock(List<OrderDetail> orderDetails) {
        for (OrderDetail detail : orderDetails) {
            ProductItem productItem = detail.getProductItem();
            if (productItem == null) {
                throw new IllegalStateException("재고 복구 실패: ProductItem이 존재하지 않습니다. orderDetailId=" + detail.getId());
            }

            if (!productItem.getId().equals(detail.getProductItem().getId())) {
                throw new IllegalStateException("재고 복구 실패: OrderDetail의 itemId와 ProductItem의 id가 일치하지 않습니다. " +
                        "orderDetailItemId=" + detail.getProductItem() + ", productItemId=" + productItem.getId());
            }

            log.info("재고 복구 진행: 상품 ID={}, 주문 수량={}", productItem.getId(), detail.getQuantity());
            productItem.restoreStock(detail.getQuantity());
            productItemRepository.save(productItem);
        }
    }

    // 주문 철회 생성(주문 생성 -> 결제 성공 -> 주문 성공 후 주문 철회 요청 처리)
    @Transactional
    public void withdrawOrder(Long orderId) {
        log.info("주문 철회 요청 수신: orderId={}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다. orderId=" + orderId));

        log.info("주문 철회 처리 진행 중: orderId={}, cancelStatus={}", order.getId(), order.getCancelStatus());
        log.info("주문 철회 전 결제 정보 확인: orderId={}, paymentId={}", order.getId(), order.getPaymentId());

        if (order.getStatus() != OrderStatus.SUCCEEDED) {
            throw new IllegalStateException("주문 철회가 불가능한 상태입니다.");
        }

        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다. orderId=" + orderId));

        order.setCancelStatus(CancelStatus.REQUESTED);
        orderRepository.save(order);

        boolean isWithdrawSuccess = false;

        if (delivery.getStatus() == DeliveryStatus.READY) {
            isWithdrawSuccess = handlePreShippingCancellation(order, delivery);
        } else if (delivery.getStatus() == DeliveryStatus.ARRIVED) {
            isWithdrawSuccess = handlePostShippingCancellation(order, delivery);
        } else {
            throw new IllegalStateException("주문 철회가 불가능한 상태입니다.");
        }

        // 철회 최종 처리 실행
        finalizeOrderWithdrawal(order, delivery, isWithdrawSuccess);
    }

    // 배송 전 주문 철회 - 결제 취소 요청
    private boolean handlePreShippingCancellation(Order order, Delivery delivery) {
        log.info("배송 전 주문 철회 처리: orderId={}", order.getId());

        delivery.setStatus(DeliveryStatus.CANCEL_REQUESTED);
        deliveryRepository.save(delivery);

        // 포인트로 환불
        refundUserPoints(order);
        return true;
    }


    // 배송 후 반품 절차 및 주문 철회 - 결제 취소 요청
    private boolean handlePostShippingCancellation(Order order, Delivery delivery) {
        log.info("배송 후 반품 철회 처리: orderId={}", order.getId());

        delivery.setStatus(DeliveryStatus.RETURN_REQUESTED);
        deliveryRepository.save(delivery);

//        delivery.setStatus(DeliveryStatus.RETURN_ARRIVED); // 반품 완료 처리는 API에서 수행하도록 분리
//        deliveryRepository.save(delivery);

        // 포인트 환불
        refundUserPoints(order);
        return true;
    }

    // 포인트 환불 로직 (사용한 포인트 복구, 적립 포인트 회수, 결제 금액을 포인트로 환불)
    private void refundUserPoints(Order order) {
        User user = order.getUser();

        // 사용한 포인트 복구 (pointUsage를 User의 point에 복구)
        Integer usedPoints = order.getPointUsage() != null ? order.getPointUsage() : 0;
        if (usedPoints > 0) {
            log.info("사용한 포인트 복구: userId={}, 복구 포인트={}", user.getUserId(), usedPoints);
            user.updatePoint(usedPoints);
        }

        // 적립된 포인트 차감 (pointSave를 User의 point에서 차감)
        Integer savedPoints = order.getPointSave() != null ? order.getPointSave() : 0;
        if (savedPoints > 0) {
            log.info("적립 포인트 차감: userId={}, 차감 포인트={}", user.getUserId(), savedPoints);
            user.updatePoint(-savedPoints);
        }

        // 결제 금액을 포인트로 환불 (paymentAmount를 User의 point로 적립)
        Long refundAmount = order.getPaymentAmount();
        if (refundAmount != null && refundAmount > 0) {
            log.info("결제 금액 포인트 환불: userId={}, 환불 포인트={}", user.getUserId(), refundAmount);
            user.updatePoint(refundAmount.intValue());
        } else {
            log.warn("환불할 결제 금액이 없습니다. orderId={}", order.getId());
        }

        userRepository.save(user);
    }


    @Transactional
    public void finalizeOrderWithdrawal(Order order, Delivery delivery, boolean isWithdrawSuccess) {
        if (isWithdrawSuccess) {
            log.info("주문 철회 성공: orderId={}", order.getId());

            order.setCancelStatus(CancelStatus.COMPLETED);
            order.setStatus(OrderStatus.WITHDRAW);

            // 재고 복구
            restoreStock(order.getOrderDetails());

            // 배송 상태 업데이트 (반품 완료 시 RETURNED 설정)
            if (delivery != null) {
                if (delivery.getStatus() == DeliveryStatus.RETURN_ARRIVED) {
                    delivery.setStatus(DeliveryStatus.RETURNED);
                } else if (delivery.getStatus() == DeliveryStatus.CANCEL_REQUESTED) {
                    delivery.setStatus(DeliveryStatus.CANCELED);
                }
                deliveryRepository.save(delivery);
            }

        } else {
            log.error("주문 철회 실패: 포인트 환불 불가 orderId={}", order.getId());
            order.setCancelStatus(CancelStatus.FAILED);
        }

        orderRepository.save(order);
    }
}