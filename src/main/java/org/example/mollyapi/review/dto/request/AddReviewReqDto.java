package org.example.mollyapi.review.dto.request;

public record AddReviewReqDto(
        Long orderDetailId, //주문 PK
        String content //리뷰 내용
) {
}
