package org.example.mollyapi.review.repository;

import org.example.mollyapi.review.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    ReviewLike findByReviewIdAndUserUserId(Long reviewId, Long userId);
    void deleteAllByReviewId(Long reviewId);
    @Transactional
    void deleteAllByReviewIdIn(List<Long> reviewIds);
}
