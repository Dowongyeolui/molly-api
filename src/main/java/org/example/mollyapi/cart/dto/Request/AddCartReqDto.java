package org.example.mollyapi.cart.dto.Request;

import jakarta.validation.constraints.NotNull;

public record AddCartReqDto(
        @NotNull
        Long itemId, //아이템 ID

        @NotNull(message = "수량 선택은 필수 값입니다.")
        Long quantity //수량
){
}
