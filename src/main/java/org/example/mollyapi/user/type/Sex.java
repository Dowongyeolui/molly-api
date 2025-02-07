package org.example.mollyapi.user.type;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.stream.Stream;

public enum Sex {
    MALE, FEMALE
    ;

    @JsonCreator
    public static  Sex parsing(String value) {
        return Stream.of(Sex.values())
                .filter( sex -> sex.toString().equals(value.toUpperCase()))
                .findFirst()
                .orElse(null);
    }
}
