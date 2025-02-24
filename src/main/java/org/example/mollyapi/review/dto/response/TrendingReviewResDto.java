package org.example.mollyapi.review.dto.response;

public record TrendingReviewResDto(
        Long reviewId, //리뷰 PK
        String content, //리뷰 내용
        String nickname, //리뷰 쓴 사용자 닉네임
        String profileImage, //리뷰 쓴 사용자 프로필
        Long productId,//상품 PK
        String createdAt, //리뷰 작성 일자
        Long count //좋아요수
) {
}
