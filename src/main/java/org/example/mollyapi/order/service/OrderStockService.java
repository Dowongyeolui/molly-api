package org.example.mollyapi.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void validateBeforePayment(Long orderId){
        System.out.println("----------------------------------재고 트랜잭션 시작----------------------------------");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(RuntimeException::new);
        // 1. 재고 확인 및 차감 (비관적 락)
        for (OrderDetail detail : order.getOrderDetails()) {
//            System.out.println("getOrderDetail: " + detail.getProductName());
            ProductItem productItem = productItemRepository.findById(detail.getProductItem().getId())
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. itemId=" + detail.getProductItem().getId()));

            // 재고 부족 체크
            if (productItem.getQuantity() < detail.getQuantity()) {
                log.warn("재고 부족 - 주문 실패: itemId={}, 요청 수량={}, 남은 재고={}", productItem.getId(), detail.getQuantity(), productItem.getQuantity());
                throw new IllegalArgumentException("재고가 부족하여 결제를 진행할 수 없습니다.");
            }

            // 재고 차감
            productItem.decreaseStock(detail.getQuantity());
            productItemRepository.save(productItem);
            System.out.println("----------------------------------재고 트랜잭션 커밋----------------------------------");
        }
    }
}