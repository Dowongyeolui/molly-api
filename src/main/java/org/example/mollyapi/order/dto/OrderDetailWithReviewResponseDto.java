package org.example.mollyapi.order.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.mollyapi.order.entity.OrderDetail;
import org.example.mollyapi.review.repository.ReviewRepository;

@Getter
@Builder
public class OrderDetailWithReviewResponseDto {
    private Long orderId;
    private Long orderDetailId;
    private Long productId;
    private Long itemId;
    private String brandName;
    private String productName;
    private String size;
    private Long price;
    private Long quantity;
    private String image;
    private String color;
    private String reviewType;

    public static OrderDetailWithReviewResponseDto from(Long userId, OrderDetail orderDetail, ReviewRepository reviewRepository) {
        return OrderDetailWithReviewResponseDto.builder()
                .orderId(orderDetail.getOrder().getId())
                .orderDetailId(orderDetail.getId())
                .productId(orderDetail.getProductItem().getProduct().getId())
                .itemId(orderDetail.getProductItem().getId())
                .brandName(orderDetail.getBrandName())
                .productName(orderDetail.getProductItem().getProduct().getProductName())
                .size(orderDetail.getProductItem().getSize())
                .price(orderDetail.getPrice())
                .quantity(orderDetail.getQuantity())
                .image(orderDetail.getProductItem().getProduct().getThumbnail().getStoredFileName())
                .color(orderDetail.getProductItem().getColor())
                .reviewType(reviewRepository.getReviewStatus(orderDetail.getId(), userId))
                .build();
    }
}
