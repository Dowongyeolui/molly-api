package org.example.mollyapi.review.dto.response;

public record MyReviewInfoDto(
        Long reviewId, //리뷰 PK
        String content, //리뷰 내용
        Long productId,//상품 PK
        String productName, //상품명
        String productUrl, //상품 이미지
        String createdAt //마지막 수정시간
) {
}
