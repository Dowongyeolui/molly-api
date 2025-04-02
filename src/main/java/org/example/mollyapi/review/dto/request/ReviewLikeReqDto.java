package org.example.mollyapi.review.dto.request;

public record ReviewLikeReqDto(
        Long reviewId, //review PK
        boolean status //좋아요 상태
) {
}