package org.example.mollyapi.delivery.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mollyapi.delivery.type.DeliveryStatus;
import org.example.mollyapi.order.entity.Order;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "delivery")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Column(nullable = false)
    private String receiverName;

    @Column(nullable = false)
    private String receiverPhone;

    @Column(nullable = false)
    private String roadAddress;

    @Column(nullable = true)
    private String numberAddress;

    @Column(nullable = false)
    private String addrDetail;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "FK_DELIVERY_ORDER"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Order order; // 주문 정보와 1:1 매핑

    public static Delivery from(Order order, String receiverName, String receiverPhone, String roadAddress, String numberAddress, String addrDetail) {
        Delivery delivery = Delivery.builder()
                .order(order)
                .status(DeliveryStatus.READY)
                .receiverName(receiverName)
                .receiverPhone(receiverPhone)
                .roadAddress(roadAddress)
                .numberAddress(numberAddress)
                .addrDetail(addrDetail)
                .build();

        order.setDelivery(delivery); // 주문과 배송 연결
        return delivery;
    }

    public void setOrder(Order order) {
        this.order = order;
        if (order != null && order.getDelivery() != this) {
            order.setDelivery(this);
        }
    }
}