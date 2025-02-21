package org.example.mollyapi.order.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.mollyapi.order.entity.OrderDetail;

@Getter
@Builder
public class OrderDetailResponseDto {
    private Long orderId;
    private Long productId;
    private Long itemId;
    private String brandName;
    private String productName;
    private String size;
    private Long price;
    private Long quantity;
    private String image;
    private String color;

    public static OrderDetailResponseDto from(OrderDetail orderDetail) {
        return OrderDetailResponseDto.builder()
                .orderId(orderDetail.getOrder().getId())
                .productId(orderDetail.getProductItem().getProduct().getId())
                .itemId(orderDetail.getProductItem().getId())
                .brandName(orderDetail.getBrandName())
                .productName(orderDetail.getProductItem().getProduct().getProductName())
                .size(orderDetail.getProductItem().getSize())
                .price(orderDetail.getPrice())
                .quantity(orderDetail.getQuantity())
                .image(orderDetail.getProductItem().getProduct().getThumbnail().getStoredFileName())
                .color(orderDetail.getProductItem().getColor())
                .build();
    }
}