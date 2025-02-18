package org.example.mollyapi.review.dto.response;

import java.util.List;

public record GetMyReviewResDto(
        MyReviewInfo myReviewInfo,
        List<String> images //리뷰 이미지
) {
}
