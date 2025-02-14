package org.example.mollyapi.cart.dto.Request;

public record UpdateCartReqDto(
        Long cartId, //장바구니 PK
        Long itemId, //변경 아이템 PK
        Long quantity //변경 수량
) {
}