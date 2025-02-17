package org.example.mollyapi.order.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mollyapi.delivery.entity.Delivery;
import org.example.mollyapi.order.type.CancelStatus;
import org.example.mollyapi.order.type.OrderStatus;
import org.example.mollyapi.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id; // pk

    @Column(name = "toss_order_id",unique = true, length = 30)
    private String tossOrderId; // 결제용 주문 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    @Column(nullable = false)
    private Long totalAmount; // 포인트 적용 전 금액

    @Column(nullable = true)
    private Long paymentAmount; // 결제된 금액

    @Column(nullable = true)
    private String paymentId; // 결제 ID

    @Column(nullable = true)
    private String paymentType; // 결제 수단

    @Column(nullable = true)
    private Integer pointUsage; // 사용한 포인트

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CancelStatus cancelStatus;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    @Column(nullable = false)
    private LocalDateTime expirationTime;

    @Column(columnDefinition = "TEXT")
    private String deliveryInfo; // JSON 형태로 배송 정보 저장

    @PrePersist
    protected void onCreate() {
        this.orderedAt = LocalDateTime.now();
        this.expirationTime = this.orderedAt.plusMinutes(10);
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void updateOrderedAt(LocalDateTime paymentTime) { // 결제 후 주문 일시 업데이트
        this.orderedAt = paymentTime;
    }

    public void markAsFailed() {
        this.status = OrderStatus.FAILED;
    }

    public void markAsCanceled(String reason) {
        this.status = OrderStatus.CANCELED;
        this.cancelStatus = CancelStatus.REQUESTED;
    }

    public void setTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void updatePaymentInfo(String paymentId, String paymentType, Long paymentAmount, Integer pointUsage) {
        this.paymentId = paymentId;
        this.paymentType = paymentType;
        this.paymentAmount = paymentAmount;
        this.pointUsage = pointUsage;
    }

    public void setDeliveryInfo(String deliveryInfo) {
        this.deliveryInfo = deliveryInfo;
    }

    public String getDeliveryInfo() {
        return this.deliveryInfo;
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
    }
}