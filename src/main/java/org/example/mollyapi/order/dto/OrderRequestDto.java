package org.example.mollyapi.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderRequestDto {
    private Long cartId;
    private Long productId;
    private Long itemId;
    private Long quantity;

    public OrderRequestDto(Long cartId, Long productId, Long itemId, Long quantity) {
        this.cartId = cartId;
        this.productId = productId;
        this.itemId = itemId;
        this.quantity = quantity;
    }
}