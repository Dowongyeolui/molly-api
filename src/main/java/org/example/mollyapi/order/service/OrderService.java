package org.example.mollyapi.order.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.delivery.entity.Delivery;
import org.example.mollyapi.delivery.repository.DeliveryRepository;
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

    public OrderResponseDto createOrder(Long userId, List<OrderRequestDto> orderRequests) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));

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

        return new OrderResponseDto(order, orderDetails);
    }

    private String generateTossOrderId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomNum = new Random().nextInt(9000) + 1000;
        return "ORD-" + timestamp + "-" + randomNum;
    }

    public void successOrder(String tossOrderId, String paymentId, String paymentType, Long paymentAmount, Integer pointUsage, String deliveryInfoJson) {
        // 주문 찾기
        Order order = orderRepository.findByTossOrderId(tossOrderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다. tossOrderId=" + tossOrderId));

        // 주문 상태 변경
        order.setStatus(OrderStatus.SUCCEEDED);

        // 사용자의 포인트 차감
        User user = order.getUser();
        if (pointUsage != null && pointUsage > 0) {
            if (user.getPoint() < pointUsage) {
                throw new IllegalArgumentException("사용자 포인트가 부족합니다.");
            }
            user.updatePoint(-pointUsage); // 포인트 차감

            userRepository.save(user);
        }

        // 결제 정보 업데이트
        order.updatePaymentInfo(paymentId, paymentType, paymentAmount, pointUsage);

        // 배송 정보 생성
        order.setDeliveryInfo(deliveryInfoJson);
        createDelivery(order);
        orderRepository.save(order); // ✅ 저장 추가
        orderRepository.flush(); // ✅ 즉시 반영

        orderRepository.save(order);
    }

    // 주문 실패: 결제 실패로 주문이 실패하는 경우
    public void failOrder(String tossOrderId) {
        Order order = orderRepository.findByTossOrderId(tossOrderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다. tossOrderId=" + tossOrderId));

        order.setStatus(OrderStatus.FAILED);

        // 재고 복구
        for (OrderDetail detail : order.getOrderDetails()) {
            ProductItem productItem = detail.getProductItem();
            if (productItem != null) {
                log.info("[Before] 재고 복구 전 - 상품 ID: {}, 기존 재고: {}, 주문 수량: {}",
                        productItem.getId(), productItem.getQuantity(), detail.getQuantity());

                productItem.restoreStock(detail.getQuantity()); // 재고 복구
                productItemRepository.save(productItem);
                productItemRepository.flush();

                log.info("[After] 재고 복구 완료 - 상품 ID: {}, 실행 후 재고: {}",
                        productItem.getId(), productItem.getQuantity());
            } else {
                log.warn("ProductItem이 null입니다. OrderDetail ID: {}", detail.getId());
            }
        }


        // 주문 데이터 삭제 (Cascade로 OrderDetail도 삭제됨)
        orderRepository.delete(order);
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

    private void createDelivery(Order order) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode deliveryInfo = objectMapper.readTree(order.getDeliveryInfo());

            String receiverName = deliveryInfo.get("receiver_name").asText();
            String receiverPhone = deliveryInfo.get("receiver_phone").asText();
            String roadAddress = deliveryInfo.get("road_address").asText();
            String numberAddress = deliveryInfo.has("number_address") ? deliveryInfo.get("number_address").asText() : null;
            String addrDetail = deliveryInfo.get("addr_detail").asText();

            // 배송 정보 생성 및 저장
            Delivery delivery = Delivery.from(order, receiverName, receiverPhone, roadAddress, numberAddress, addrDetail);
            deliveryRepository.save(delivery);
            deliveryRepository.flush();

            // Order에 delivery_id 저장, delivery_info 제거
            order.setDelivery(delivery);
            order.setDeliveryInfo(null);
            orderRepository.save(order);
            orderRepository.flush();

            log.info("배송 생성 완료. 주문번호: {}, 배송번호: {}", order.getId(), delivery.getId());

        } catch (Exception e) {
            log.error("배송 정보 파싱 실패: {}", e.getMessage());
            throw new RuntimeException("배송 정보를 저장할 수 없습니다.");
        }
    }

}