package org.example.mollyapi.review.dto.response;

public record MyReviewInfo(
        Long reviewId, //리뷰 PK
        String content, //리뷰 내용
        Long productId,//상품 PK
        String productName, //상품명
        String productUrl //상품 이미지
) {
}
