package org.example.mollyapi.product.dto.response;

import org.example.mollyapi.product.entity.ProductItem;

import java.util.List;

public record ProductItemResDto(
        Long id,
        String color,
        String colorCode,
        String size,
        Long quantity
) {
    static public ProductItemResDto from(ProductItem productItem) {
        return new ProductItemResDto(
                productItem.getId(),
                productItem.getColor(),
                productItem.getColorCode(),
                productItem.getSize(),
                productItem.getQuantity()
        );
    }
}
