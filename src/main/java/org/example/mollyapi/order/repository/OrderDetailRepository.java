package org.example.mollyapi.order.repository;

import org.example.mollyapi.order.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findByOrderId(Long orderId);

    @Transactional
    @Modifying
    @Query("DELETE FROM OrderDetail od WHERE od.order.id = :orderId")
    void deleteByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT od.id FROM OrderDetail od WHERE od.order.id IN :orderIds")
    List<Long> findOrderDetailIdsByOrderIds(@Param("orderIds") List<Long> orderIds);

    @Modifying
    @Transactional
    @Query("DELETE FROM OrderDetail od WHERE od.order.id IN :orderIds")
    int deleteAllByOrderIds(@Param("orderIds") List<Long> orderIds);
}