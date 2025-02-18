package org.example.mollyapi.product.dto;

import jakarta.validation.constraints.NotBlank;
import org.example.mollyapi.product.entity.ProductItem;

public record ProductItemDto(
        Long id,
        @NotBlank String color,
        @NotBlank String colorCode,
        @NotBlank String size,
        @NotBlank Long quantity
) {

    public static ProductItemDto from(ProductItem productItem) {
        return new ProductItemDto(
                productItem.getId(),
                productItem.getColor(),
                productItem.getColorCode(),
                productItem.getSize(),
                productItem.getQuantity()
        );
    }
}
