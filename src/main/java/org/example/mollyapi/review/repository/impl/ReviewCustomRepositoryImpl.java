package org.example.mollyapi.review.repository.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.mollyapi.review.dto.response.MyReviewInfo;
import org.example.mollyapi.review.dto.response.ReviewInfo;
import org.example.mollyapi.review.repository.ReviewCustomRepository;

import java.util.List;

import static org.example.mollyapi.product.entity.QProductImage.productImage;
import static org.example.mollyapi.user.entity.QUser.user;
import static org.example.mollyapi.review.entity.QReview.review;
import static org.example.mollyapi.review.entity.QReviewLike.reviewLike;
import static org.example.mollyapi.review.entity.QReviewImage.reviewImage;

@RequiredArgsConstructor
public class ReviewCustomRepositoryImpl implements ReviewCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ReviewInfo> getReviewInfo(Long productId, Long userId) {
        return jpaQueryFactory.select(
                        Projections.constructor(ReviewInfo.class,
                                review.id,
                                review.content,
                                user.nickname,
                                user.profileImage,
                                reviewLike.isLike.coalesce(Boolean.FALSE).as("isLike")
                        )).from(review)
                .innerJoin(review.user, user)
                .leftJoin(reviewLike).on(review.id.eq(reviewLike.review.id)
                        .and(reviewLike.user.userId.eq(userId)))
                .where(review.product.id.eq(productId)
                        .and(review.isDeleted.eq(Boolean.FALSE)))
                .fetch();
    }

    @Override
    public List<String> getImageList(Long reviewId) {
        return jpaQueryFactory.select(
                    reviewImage.url
                ).from(reviewImage)
                .where(reviewImage.review.id.eq(reviewId))
                .fetch();
    }

    @Override
    public List<MyReviewInfo> getMyReviewInfo(Long userId) {
        return jpaQueryFactory.select(
                Projections.constructor(MyReviewInfo.class,
                        review.id,
                        review.content,
                        review.product.id,
                        review.product.productName.coalesce("게시 중단된 상품입니다."),
                        productImage.url.coalesce("게시 중단된 상품입니다.")
                )).from(review)
                .innerJoin(productImage).on(review.product.id.eq(productImage.product.id)
                        .and(productImage.isRepresentative.eq(Boolean.TRUE)))
                .where(review.isDeleted.eq(Boolean.FALSE)
                        .and(review.user.userId.eq(userId)))
                .fetch();
    }
}
