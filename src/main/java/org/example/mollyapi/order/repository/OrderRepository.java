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

    @Query("SELECT o FROM Order o JOIN FETCH o.orderDetails WHERE o.tossOrderId = :tossOrderId")
    Optional<Order> findByTossOrderIdWithDetails(@Param("tossOrderId") String tossOrderId);

    Optional<Order> findByTossOrderId(String tossOrderId);
    List<Order> findByUserAndStatusIn(@Param("user") User user, @Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.status IN (:statuses)")
    List<Order> findOrdersByUserAndStatusIn(@Param("user") User user, @Param("statuses") List<OrderStatus> statuses);

    default Order findOrderById(Long orderId) {
        return findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다. orderId=" + orderId));
    }

    @Query("SELECT o FROM Order o WHERE o.expirationTime < :now AND o.status = 'PENDING'")
    List<Order> findExpiredPendingOrders(@Param("now") LocalDateTime now);
}