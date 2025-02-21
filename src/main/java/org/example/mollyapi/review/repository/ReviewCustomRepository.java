package org.example.mollyapi.review.repository;

import org.example.mollyapi.review.dto.response.MyReviewInfoDto;
import org.example.mollyapi.review.dto.response.ReviewInfoDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewCustomRepository {
    List<ReviewInfoDto> getReviewInfo(Pageable pageable, Long productId, Long userId);
    List<String> getImageList(Long reviewId);
    List<MyReviewInfoDto> getMyReviewInfo(Pageable pageable, Long userId);
    String getReviewStatus(Long orderDetailId, Long userId);
}
