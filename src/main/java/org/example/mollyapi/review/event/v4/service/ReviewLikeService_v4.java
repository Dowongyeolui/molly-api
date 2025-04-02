package org.example.mollyapi.review.event.v4.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.review.entity.Review;
import org.example.mollyapi.review.entity.ReviewLike;
import org.example.mollyapi.review.event.v3.key.RedisKey;
import org.example.mollyapi.review.repository.ReviewLikeRepository;
import org.example.mollyapi.review.service.ReviewService;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.service.UserService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.example.mollyapi.common.exception.error.impl.ReviewLikeError.EXIST_LIKE;
import static org.example.mollyapi.common.exception.error.impl.ReviewLikeError.NOT_EXIST_LIKE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewLikeService_v4 {
    private final UserService userService;
    private final ReviewService reviewService;
    private final ReviewLikeRepository reviewLikeRep;
    private final StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> ops;
    private Long last = 0L;

    @Transactional
    public void addReviewLike(Long reviewId, Long userId) {
        ops = redisTemplate.opsForValue();

        // 사용자 및 리뷰 여부 체크
        User user = userService.findByUser(userId);
        Review review = reviewService.findByReviewWithWriteLock(reviewId);
        ReviewLike reviewLike = reviewLikeRep.findByReviewIdAndUserId(reviewId, userId);

        // 이미 좋아요를 눌렀다면 예외 발생
        if(reviewLike != null) throw new CustomException(EXIST_LIKE);

        // Redis에서 사용할 키 생성
        String formatLike = String.format(RedisKey.REVIEW_LIKE.getValue() + review.getId());

        if(ops.get(formatLike) == null) {
            log.error("Redis Key IS NULL. Save current reviewLike");
            if(review.getLikeCount() < last) { // 데이터 정합성을 위한 연산
                ops.set(formatLike, String.valueOf(last - review.getLikeCount()));
            }
        }

        // Lettuce 트랜잭션을 이용한 원자적 증가
        last = ops.increment(formatLike, 1);
        buildPostLike(user, review); //좋아요 생성
    }

    @Transactional
    public void removeReviewLike(Long reviewId, Long userId) {
        ops = redisTemplate.opsForValue();

        // 사용자 및 리뷰 여부 체크
        userService.validUser(userId);
        Review review = reviewService.findByReviewWithWriteLock(reviewId);
        ReviewLike reviewLike = reviewLikeRep.findByReviewIdAndUserId(reviewId, userId);

        // 좋아요가 존재하지 않는 경우 예외 발생
        if (reviewLike == null) throw new CustomException(NOT_EXIST_LIKE);

        // Redis에서 사용할 키 생성
        String formatLike = String.format(RedisKey.REVIEW_LIKE.getValue() + review.getId());

        // Redis에서 좋아요 감소
        ops.decrement(formatLike, review.getLikeCount()-1);

        // 사용자별 좋아요 데이터 삭제
        redisTemplate.delete(formatLike + ":user:" + userId);

        reviewLikeRep.delete(reviewLike);
    }

    private void buildPostLike(User user, Review review) {
        ReviewLike reviewLike = ReviewLike.builder()
                .user(user)
                .review(review)
                .build();

        reviewLikeRep.save(reviewLike);
    }
}
