package org.example.mollyapi.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.mollyapi.order.entity.OrderDetail;

public record OrderDetailResponseDto(
        @JsonProperty("orderId") Long orderId,
        @JsonProperty("productId") Long productId,
        @JsonProperty("itemId") Long itemId,
        @JsonProperty("brandName") String brandName,
        @JsonProperty("productName") String productName,
        @JsonProperty("size") String size,
        @JsonProperty("price") Long price,
        @JsonProperty("quantity") Long quantity,
        @JsonProperty("image") String image,
        @JsonProperty("color") String color
) {
    public static OrderDetailResponseDto from(OrderDetail orderDetail) {
        return new OrderDetailResponseDto(
                orderDetail.getOrder().getId(),
                orderDetail.getProductItem().getProduct().getId(),
                orderDetail.getProductItem().getId(),
                orderDetail.getBrandName(),
                orderDetail.getProductItem().getProduct().getProductName(),
                orderDetail.getProductItem().getSize(),
                orderDetail.getPrice(),
                orderDetail.getQuantity(),
                orderDetail.getProductItem().getProduct().getThumbnail().getStoredFileName(),
                orderDetail.getProductItem().getColor()
        );
    }
}