package org.example.mollyapi.review.event.v4.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.review.entity.Review;
import org.example.mollyapi.review.repository.ReviewRepository;
import org.example.mollyapi.review.service.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewUpdateService_v4 {
    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;

    @Transactional
    public Review save(Long reviewId, Long likeCount) {
        Review review = reviewService.findByReviewWithWriteLock(reviewId);
        review.updateLikeCount(review.getLikeCount()+likeCount);

        Review savedReview = reviewRepository.save(review);
        reviewRepository.flush();  // 즉시 반영
        return savedReview;
    }
}
