package org.example.mollyapi.product.dto;

import java.util.List;

public record ProductDto(
        Long id,
        List<String> categories,
        String brandName,
        String productName,
        Long price,
        String description
) {
}
