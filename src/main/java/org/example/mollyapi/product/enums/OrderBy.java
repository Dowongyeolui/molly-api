package org.example.mollyapi.product.enums;

import lombok.Getter;

@Getter
public enum OrderBy {
    CREATED_AT("createdAt"),
    VIEW_COUNT("viewCount"),
    PURCHASE_COUNT("purchaseCount"),
    PRICE_DESC("priceDesc"),
    PRICE_ASC("priceAsc");

    private final String value;

    OrderBy(String value) {
        this.value = value;
    }
}
