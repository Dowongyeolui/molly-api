package org.example.mollyapi.product.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductImageType {
    THUMBNAIL("thumbnail"),
    PRODUCT("product"),
    DESCRIPTION("description");
    ;

    private final String value;
}
