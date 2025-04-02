package org.example.mollyapi.review.event.v3.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.review.entity.Review;
import org.example.mollyapi.review.entity.ReviewLike;
import org.example.mollyapi.review.repository.ReviewLikeRepository;
import org.example.mollyapi.user.entity.User;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewLikeWriteService_v3 {
    private final ReviewLikeRepository reviewLikeRepository;

    public void addReviewLike(ValueOperations<String, String> ops, String formatLike, User user, Review review) {

        // 좋아요 값이 없으면, 누적 좋아요 +1
        if (ops.get(formatLike) == null){
            ops.set(formatLike, String.valueOf(review.getLikeCount()+1));
            log.info("v3 likeCount 1={}", review.getLikeCount());
        }else {
            ops.increment(formatLike);
            log.info("v3 likeCount 2={}", ops.get(formatLike));
        }
        buildPostLike(user, review);
    }

    public void removeReviewLike(ValueOperations<String, String> ops, String formatLike, Review review, ReviewLike reviewLike) {
        // 좋아요 값이 있으면, 누적 좋아요 -1
        if (ops.get(formatLike) == null){
            ops.set(formatLike, Long.toString(review.getLikeCount()-1));
        }else {
            ops.decrement(formatLike);
        }
        reviewLikeRepository.delete(reviewLike);
    }

    private void buildPostLike(User user, Review review) {
        ReviewLike reviewLike = ReviewLike.builder()
                .user(user)
                .review(review)
                .build();

        reviewLikeRepository.save(reviewLike);
    }
}
