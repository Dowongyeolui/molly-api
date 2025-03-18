package org.example.mollyapi.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.cart.repository.CartRepository;
import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.order.entity.OrderDetail;
import org.example.mollyapi.order.repository.OrderRepository;
import org.example.mollyapi.product.entity.ProductItem;
import org.example.mollyapi.product.repository.ProductItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStockService {

    private final ProductItemRepository productItemRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    //    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Transactional(propagation = Propagation.REQUIRED) // 기존 트랜잭션을 유지
    public void validateBeforePayment(Long orderId) {
        System.out.println("----------------------------------재고 트랜잭션 시작----------------------------------");
        log.info("orderId = {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("validateBeforePayment: 일치하는 주문이 없습니다."));
        log.info("order = {}", order.getOrderDetails());
        // 1. 재고 확인 및 차감 (비관적 락)
        for (OrderDetail detail : order.getOrderDetails()) {
            ProductItem productItem = productItemRepository.findByIdWithLock(detail.getProductItem().getId())
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. itemId=" + detail.getProductItem().getId()));
            log.info("productItem.getQuantity = {}", productItem.getQuantity());
            // 재고 부족 체크
//            if (productItem.getQuantity() < detail.getQuantity()) {
//                log.warn("재고 부족 - 주문 실패: itemId={}, 요청 수량={}, 남은 재고={}", productItem.getId(), detail.getQuantity(), productItem.getQuantity());
//                throw new IllegalArgumentException("재고가 부족하여 결제를 진행할 수 없습니다.");
//            }

            // 재고 차감
            productItem.decreaseStock(detail.getQuantity());
            System.out.println("----------------------------------재고 트랜잭션 커밋----------------------------------");
        }

        // 2. 장바구니 삭제
        for (OrderDetail orderDetail : order.getOrderDetails()) {
            if (orderDetail.getCartId() != null) {
                cartRepository.deleteById(orderDetail.getCartId());
            }
        }
    }
}

//package org.example.mollyapi.order.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.example.mollyapi.cart.repository.CartRepository;
//import org.example.mollyapi.order.entity.Order;
//import org.example.mollyapi.order.entity.OrderDetail;
//import org.example.mollyapi.order.repository.OrderRepository;
//import org.example.mollyapi.product.entity.ProductItem;
//import org.example.mollyapi.product.repository.ProductItemRepository;
//import org.springframework.orm.ObjectOptimisticLockingFailureException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class OrderStockService {
//
//    private final ProductItemRepository productItemRepository;
//    private final OrderRepository orderRepository;
//    private final CartRepository cartRepository;
//
//
//    @Transactional
//    public void validateBeforePayment(Long orderId) {
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new IllegalArgumentException("validateBeforePayment: 일치하는 주문이 없습니다."));
//
//        for (OrderDetail detail : order.getOrderDetails()) {
//            boolean success = false;
//            int retryCount = 0;
//            while (!success && retryCount < 3) { // 낙관적 락 충돌 시 3번 재시도
//                try {
//                    ProductItem productItem = productItemRepository.findByIdWithOptimisticLock(detail.getProductItem().getId())
//                            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. itemId=" + detail.getProductItem().getId()));
//
//                    log.info("현재 재고 = {}", productItem.getQuantity());
//
//                    // 재고 차감 후 강제 저장
//                    productItem.decreaseStock(detail.getQuantity());
//                    productItemRepository.save(productItem); // ✅ JPA 변경 감지 강제 수행
//
//                    success = true;
//                    log.info("재고 차감 성공: 상품 ID={}, 남은 재고={}", productItem.getId(), productItem.getQuantity());
//
//                } catch (ObjectOptimisticLockingFailureException e) {
//                    retryCount++;
//                    log.warn("낙관적 락 충돌 발생 - 재시도 {}/3, orderId={}", retryCount, orderId);
//                    if (retryCount == 3) {
//                        throw new IllegalArgumentException("재고가 부족하여 결제를 진행할 수 없습니다.");
//                    }
//                }
//            }
//        }
//
//        // 장바구니 삭제
//        for (OrderDetail orderDetail : order.getOrderDetails()) {
//            if (orderDetail.getCartId() != null) {
//                cartRepository.deleteById(orderDetail.getCartId());
//            }
//        }
//    }
//}