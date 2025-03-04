package org.example.mollyapi.delivery.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mollyapi.delivery.dto.DeliveryReqDto;
import org.example.mollyapi.delivery.type.DeliveryStatus;
import org.example.mollyapi.order.entity.Order;

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

    @Column
    private String numberAddress;

    @Column(nullable = false)
    private String addrDetail;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "FK_DELIVERY_ORDER"))
    private Order order;

    public static Delivery from(DeliveryReqDto deliveryInfo) {
        return Delivery.builder()
                .receiverName(deliveryInfo.receiver_name())
                .receiverPhone(deliveryInfo.receiver_phone())
                .roadAddress(deliveryInfo.road_address())
                .numberAddress(deliveryInfo.number_address())
                .addrDetail(deliveryInfo.addr_detail())
                .status(DeliveryStatus.READY) // 기본값 설정
                .build();
    }

    public void assignOrder(Order order) {
        this.order = order;
    }
}