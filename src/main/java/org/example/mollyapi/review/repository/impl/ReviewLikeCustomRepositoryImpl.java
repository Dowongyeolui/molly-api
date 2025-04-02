package org.example.mollyapi.review.repository.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.example.mollyapi.review.entity.ReviewLike;
import org.example.mollyapi.review.repository.ReviewLikeCustomRepository;

import static org.example.mollyapi.review.entity.QReviewLike.reviewLike;

@RequiredArgsConstructor
public class ReviewLikeCustomRepositoryImpl implements ReviewLikeCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public ReviewLike findByReviewIdAndUserId(Long reviewId, Long userId) {
        return jpaQueryFactory.selectFrom(reviewLike)
                .where(reviewLike.review.id.eq(reviewId)
                        .and(reviewLike.user.userId.eq(userId)))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne();
    }
}