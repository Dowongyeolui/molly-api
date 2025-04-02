package org.example.mollyapi.review.repository;

import org.example.mollyapi.review.entity.ReviewLike;

public interface ReviewLikeCustomRepository {
    ReviewLike findByReviewIdAndUserId(Long reviewId, Long userId);
}
