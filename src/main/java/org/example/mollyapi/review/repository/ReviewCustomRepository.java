package org.example.mollyapi.review.repository;

import org.example.mollyapi.review.dto.response.MyReviewInfo;
import org.example.mollyapi.review.dto.response.ReviewInfo;

import java.util.List;
public interface ReviewCustomRepository {
    List<ReviewInfo> getReviewInfo(Long productId, Long userId);
    List<String> getImageList(Long reviewId);
    List<MyReviewInfo> getMyReviewInfo(Long userId);
}
