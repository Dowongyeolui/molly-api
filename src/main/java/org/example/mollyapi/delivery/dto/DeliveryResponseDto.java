package org.example.mollyapi.delivery.dto;

import lombok.Getter;
import org.example.mollyapi.delivery.entity.Delivery;

@Getter
public class DeliveryResponseDto {
    private final Long deliveryId;
    private final String addrDetail;
    private final String numberAddress;
    private final String receiverName;
    private final String receiverPhone;
    private final String roadAddress;
    private final String status;

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