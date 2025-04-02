package org.example.mollyapi.review.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.review.dto.request.ReviewLikeReqDto;
import org.example.mollyapi.review.entity.Review;
import org.example.mollyapi.review.entity.ReviewLike;
import org.example.mollyapi.review.repository.ReviewLikeRepository;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewLikeService {
    private final UserService userService;
    private final ReviewService reviewService;
    private final ReviewLikeRepository reviewLikeRep;

    /**
     * 좋아요 상태 변경
     * @param likeDto 리뷰 Pk, 좋아요 상태를 담은 Dto
     * @param userId 사용자 PK
     * */
    @Transactional
    public void changeReviewLike(ReviewLikeReqDto likeDto, Long userId) {
        // 사용자 및 리뷰 여부 체크
        User user = userService.findByUser(userId);
        Review review = reviewService.findByReviewWithWriteLock(likeDto.reviewId());

        // 사용자가 이전에 누른 좋아요가 있는 지
        ReviewLike reviewLike = reviewLikeRep.findByReviewIdAndUserId(review.getId(), userId);

        if (reviewLike == null && likeDto.status()) {
            // 좋아요 생성
            reviewLike = ReviewLike.builder()
                    .user(user)
                    .review(review)
                    .build();

            reviewLikeRep.save(reviewLike);
            review.increaseLikeCount();
        } else if (reviewLike != null && !likeDto.status()) { // 이미 좋아요를 눌렀던 적이 있을 경우
            reviewLikeRep.delete(reviewLike);
            review.decreaseLikeCount();
        }
    }
}
