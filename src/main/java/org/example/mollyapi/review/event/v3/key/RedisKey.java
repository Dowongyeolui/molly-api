package org.example.mollyapi.review.event.v3.key;

import lombok.Getter;

@Getter
public enum RedisKey {

    REVIEW_LIKE("reviewLike:");

    private final String value; //필드 추가

    RedisKey(String value) {
        this.value = value;
    }
}

