package org.example.mollyapi.cart.dto.Response;

public record CartInfoDto(
        Long cartId, //장바구니 PK
        Long itemId, //아이템 PK
        String color, //색상
        String size, //사이즈
        Long productId,//상품 PK
        String productName, //상품명
        String brandName, //브랜드명
        Long price, //상품 가격
        String url, //상품 썸네일 이미지
        Long quantity //장바구니에 담긴 수량
) {
}
