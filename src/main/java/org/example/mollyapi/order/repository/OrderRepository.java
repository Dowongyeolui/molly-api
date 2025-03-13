package org.example.mollyapi.order.repository;

import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.order.type.OrderStatus;
import org.example.mollyapi.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByTossOrderId(String tossOrderId);

    boolean existsByTossOrderId(String tossOrderId);

    List<Order> findByUserAndStatusIn(@Param("user") User user, @Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.status IN (:statuses)")
    List<Order> findOrdersByUserAndStatusIn(@Param("user") User user, @Param("statuses") List<OrderStatus> statuses);

    default Order findOrderById(Long orderId) {
        return findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다. orderId=" + orderId));
    }

    @Query("SELECT o FROM Order o WHERE o.expirationTime < :now AND o.status = 'PENDING'")
    List<Order> findExpiredPendingOrders(@Param("now") LocalDateTime now);

    // 중복 주문 방지: 특정 사용자가 진행 중인 주문이 있는지 확인
    @Query("SELECT COUNT(o) > 0 FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    boolean existsByUserIdAndStatus(@Param("userId") Long userId, @Param("status") OrderStatus status);

    // 동일 상품 중복 주문 방지: 특정 사용자가 특정 상품을 몇 번 주문했는지 확인
    @Query("SELECT COUNT(o) FROM OrderDetail o WHERE o.order.user = :user AND o.productItem.id = :productItemId")
    long countByUserAndProductItem(@Param("user") User user, @Param("productItemId") Long productItemId);
}