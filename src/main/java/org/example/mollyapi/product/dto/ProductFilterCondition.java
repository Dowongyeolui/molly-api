package org.example.mollyapi.product.dto;

import lombok.Builder;
import org.example.mollyapi.product.enums.OrderBy;

import java.util.List;

@Builder
public record ProductFilterCondition (
    List<String> colorCode,
    List<String> size,
    List<Long> categoryId,
    String brandName,
    Long priceGoe,
    Long priceLt,
    Long sellerId,
    Boolean excludeSoldOut,
    OrderBy orderBy
    ) {
    public static ProductFilterCondition of(
            List<String> colorCode,
            List<String> size,
            List<Long> categoryId,
            String brandName,
            Long priceGoe,
            Long priceLt,
            Long sellerId,
            OrderBy orderBy,
            Boolean excludeSoldOut
    )  {
        return ProductFilterCondition.builder()
                .colorCode(colorCode)
                .size(size)
                .categoryId(categoryId)
                .brandName(brandName)
                .priceGoe(priceGoe)
                .priceLt(priceLt)
                .sellerId(sellerId)
                .excludeSoldOut(excludeSoldOut)
                .orderBy(orderBy)
                .build();
    }
}
