package org.example.mollyapi.order.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderRequestDto {
    private Long cartId;
    private Long itemId;
    private Long quantity;

    @JsonCreator
    public OrderRequestDto(
            @JsonProperty("cartId") Long cartId,
            @JsonProperty("itemId") Long itemId,
            @JsonProperty("quantity") Long quantity) {
        this.cartId = cartId;
        this.itemId = itemId;
        this.quantity = quantity;

        if (cartId != null && (itemId != null || quantity != null)) {
            throw new IllegalArgumentException("장바구니 주문이므로 itemId와 quantity는 포함될 수 없습니다.");
        }
        if (cartId == null && (itemId == null || quantity == null)) {
            throw new IllegalArgumentException("바로 주문이므로 itemId와 quantity는 필수입니다.");
        }
    }
}