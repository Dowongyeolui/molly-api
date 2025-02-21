package org.example.mollyapi.product.dto.request;

import org.example.mollyapi.product.enums.OrderBy;
import org.springframework.web.bind.annotation.RequestParam;

public record ProductFilterConditionReqDto(
        @RequestParam(required = false) String colorCode,
        @RequestParam(required = false) String productSize,
        @RequestParam(required = false) String categories,
        @RequestParam(required = false) String brandName,
        @RequestParam(required = false) Long priceGoe,
        @RequestParam(required = false) Long priceLt,
        @RequestParam(required = false) OrderBy orderBy,
        @RequestParam(required = false) Boolean excludeSoldOut
) {
}
