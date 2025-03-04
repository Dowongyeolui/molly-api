package org.example.mollyapi.order.dto;

import jakarta.validation.constraints.NotNull;

public record OrderRequestDto(
        Long cartId,
        @NotNull Long itemId,
        @NotNull Long quantity
) {
    public static OrderRequestDto from(Long cartId, Long itemId, Long quantity) {
        return new OrderRequestDto(cartId, itemId, quantity);
    }
}