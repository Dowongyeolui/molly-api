package org.example.mollyapi.cart.dto.Response;

import org.example.mollyapi.product.dto.response.ProductResDto;

import java.util.List;

public record CartInfoResDto(
        CartInfoDto cartInfoDto, //장바구니에 담긴 상품 정보
        List<ProductResDto.ColorDetail> colorDetails //상품의 색상 및 사이즈 옵셥
) {
}
