package org.example.mollyapi.review.dto.response;

public record ReviewInfo(
        Long reviewId, //리뷰 PK
        String content, //리뷰 내용
        String nickname, //리뷰 쓴 사용자 닉네임
        String profileImage, //리뷰 쓴 사용자 프로필
        boolean isLike //좋아요 유무
) {
}
