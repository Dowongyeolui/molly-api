package org.example.mollyapi.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.example.mollyapi.product.entity.ProductItem;

public record ProductItemReqDto(
        Long id,
        @NotBlank String color,
        @NotBlank String colorCode,
        @NotBlank String size,
        @NotBlank Long quantity
) {
    static public ProductItem toEntity(ProductItemReqDto productItemReqDto) {
        return ProductItem.builder()
                .id(productItemReqDto.id)
                .color(productItemReqDto.color)
                .colorCode(productItemReqDto.colorCode)
                .size(productItemReqDto.size)
                .quantity(productItemReqDto.quantity)
                .build();
    }
}
