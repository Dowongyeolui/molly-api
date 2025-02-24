package org.example.mollyapi.review.service;

import lombok.RequiredArgsConstructor;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.review.dto.response.GetTrendingReviewResDto;
import org.example.mollyapi.review.dto.response.TrendingReviewResDto;
import org.example.mollyapi.review.repository.ReviewRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.example.mollyapi.common.exception.error.impl.ReviewError.NOT_FOUND_REVIEW;

@Service
@RequiredArgsConstructor
public class TrendingReviewService {
    private final ReviewRepository reviewRep;

    /**
     * 최근 7일 간 인기 리뷰 조회
     * */
    @Transactional(readOnly = true)
    public ResponseEntity<?> getTrendingReview() {
        List<TrendingReviewResDto> trendingReviewList = reviewRep.getTrendingReviewInfo()
                .orElseThrow(() -> new CustomException(NOT_FOUND_REVIEW));

        // Response로 전달할 인기 리뷰 정보 담기
        List<GetTrendingReviewResDto> reviewResDtoList = new ArrayList<>();
        for(TrendingReviewResDto info : trendingReviewList) {
            List<String> images = reviewRep.getImageList(info.reviewId());
            if (images.isEmpty()) continue;

            // 리뷰 이미지 정보를 DTO에 추가
            reviewResDtoList.add(new GetTrendingReviewResDto(info, images));
        }
        return ResponseEntity.ok().body(reviewResDtoList);
    }
}
