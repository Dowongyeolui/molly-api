package org.example.mollyapi.common.enums;

import lombok.Getter;

@Getter
public enum ImageType {
    PRODUCT("product"),
    REVIEW("review");

    private final String value;

    ImageType(String value) {
        this.value = value;
    }
}
