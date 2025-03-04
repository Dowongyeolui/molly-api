package org.example.mollyapi.cart.dto.Request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateCartReqDto(
        @NotNull
        Long cartId, //장바구니 PK

        @NotNull
        Long itemId, //변경 아이템 PK

        @Positive(message = "수량은 1개 이상부터 가능합니다.")
        Long quantity //변경 수량
) {
}