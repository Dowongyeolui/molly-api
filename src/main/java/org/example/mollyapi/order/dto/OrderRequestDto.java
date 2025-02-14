package org.example.mollyapi.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderRequestDto {
    private Long productId;
    private Long itemId;
    private Long quantity;

    public OrderRequestDto(Long productId, Long itemId, Long quantity) {
        this.productId = productId;
        this.itemId = itemId;
        this.quantity = quantity;
    }
}