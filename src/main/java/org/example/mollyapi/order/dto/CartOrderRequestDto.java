package org.example.mollyapi.order.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CartOrderRequestDto {
    private Long cartId;

    @JsonCreator
    public CartOrderRequestDto(@JsonProperty("cartId") Long cartId) {
        if (cartId == null) {
            throw new IllegalArgumentException("cartId는 필수입니다.");
        }
        this.cartId = cartId;
    }
}
