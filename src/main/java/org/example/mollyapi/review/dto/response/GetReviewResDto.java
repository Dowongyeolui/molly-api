package org.example.mollyapi.review.dto.response;

import java.util.List;

public record GetReviewResDto(
        ReviewInfoDto reviewInfo,
        List<String> images //리뷰 이미지
) {
}
