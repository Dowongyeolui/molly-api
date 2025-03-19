package org.example.mollyapi.cart.dto.Request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddCartReqDto(
        @NotNull
        Long itemId, //아이템 ID

        @Positive(message = "수량은 1개 이상부터 가능합니다.")
        Long quantity //수량
){
}
