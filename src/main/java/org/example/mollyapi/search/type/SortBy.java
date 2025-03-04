package org.example.mollyapi.search.type;

import java.util.stream.Stream;

public enum SortBy {
    PRICE_DESC, PRICE_ASC,
    NEW_DESC, NEW_ASC,
    VIEW_DESC, VIEW_ASC,
    SELL_DESC, SELL_ASC
    ;

    public static SortBy parsing(String value) {
        return Stream.of(SortBy.values())
                .filter( sortBy -> sortBy.toString().equals(value.toUpperCase()))
                .findFirst()
                .orElse(null);
    }
}
