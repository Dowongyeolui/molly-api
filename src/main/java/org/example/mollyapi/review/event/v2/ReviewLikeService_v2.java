package org.example.mollyapi.review.event.v2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.review.entity.Review;
import org.example.mollyapi.review.entity.ReviewLike;
import org.example.mollyapi.review.event.v2.event.LikeCountEvent;
import org.example.mollyapi.review.repository.ReviewLikeRepository;
import org.example.mollyapi.review.service.ReviewService;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import static org.example.mollyapi.common.exception.error.impl.ReviewLikeError.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewLikeService_v2 {
    private final UserService userService;
    private final ReviewService reviewService;
    private final ReviewLikeRepository reviewLikeRep;
    private final ReviewLikeEventBlockQueue reviewLikeEventBlockQueue;


    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void addReviewLike(Long reviewId, Long userId) {
        // 사용자 및 리뷰 여부 체크
        User user = userService.findByUser(userId);
        Review review = reviewService.findByReview(reviewId);
        ReviewLike reviewLike = reviewLikeRep.findByReviewIdAndUserId(reviewId, userId);

        if(reviewLike != null) throw new CustomException(EXIST_LIKE);

        reviewLike = ReviewLike.builder()
                .user(user)
                .review(review)
                .build();

        reviewLikeRep.save(reviewLike);

        reviewLikeEventBlockQueue.publishEvent(new LikeCountEvent(reviewId, userId, LikeCountEvent.LikeAction.INCREASE));
    }

    @Transactional
    public void removeReviewLike(Long reviewId, Long userId) {
        // 사용자 및 리뷰 여부 체크
        userService.validUser(userId);
        Review review = reviewService.findByReview(reviewId);
        ReviewLike reviewLike = reviewLikeRep.findByReviewIdAndUserId(reviewId, userId);

        if (reviewLike == null) throw new CustomException(NOT_EXIST_LIKE);
        reviewLikeRep.delete(reviewLike);

        reviewLikeEventBlockQueue.publishEvent(new LikeCountEvent(reviewId, userId, LikeCountEvent.LikeAction.DECREASE));
    }
}
