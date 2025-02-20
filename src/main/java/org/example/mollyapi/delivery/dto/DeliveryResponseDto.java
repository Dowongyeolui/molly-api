package org.example.mollyapi.delivery.dto;

import lombok.Getter;
import org.example.mollyapi.delivery.entity.Delivery;

@Getter
public class DeliveryResponseDto {
    private Long deliveryId;
    private String addrDetail;
    private String numberAddress;
    private String receiverName;
    private String receiverPhone;
    private String roadAddress;
    private String status;

    public static DeliveryResponseDto from(Delivery delivery) {
        return new DeliveryResponseDto(delivery);
    }

    private DeliveryResponseDto(Delivery delivery) {
        this.deliveryId = delivery.getId();
        this.addrDetail = delivery.getAddrDetail();
        this.numberAddress = delivery.getNumberAddress();
        this.receiverName = delivery.getReceiverName();
        this.receiverPhone = delivery.getReceiverPhone();
        this.roadAddress = delivery.getRoadAddress();
        this.status = delivery.getStatus().name();
    }
}