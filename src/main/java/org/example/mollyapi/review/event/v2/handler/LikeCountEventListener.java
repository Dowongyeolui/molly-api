package org.example.mollyapi.review.event.v2.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.review.entity.Review;
import org.example.mollyapi.review.event.v2.event.LikeCountEvent;
import org.example.mollyapi.review.repository.ReviewRepository;
import org.example.mollyapi.review.service.ReviewService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeCountEventListener {
    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;

    @Async("reviewLikeExecutor") //비동기 유지
    @Transactional(propagation = Propagation.REQUIRES_NEW) //전파 유도 방식
    @TransactionalEventListener //해당 트랜잭션이 성공적으로 마쳤을 경우에만 이벤트를 처리
    public void handleReviewIncreaseLikeCountEvent(LikeCountEvent event) {
        log.info("이벤트 실행: reviewId={} LikeAction={}", event.reviewId(), event.action());

        try {
            // 리뷰 엔티티 조회
            Review review = reviewService.findByReviewWithWriteLock(event.reviewId());

            // 좋아요 수 증가 또는 감소 처리
            switch (event.action()) {
                case INCREASE:
                    review.increaseLikeCount();
                    break;
                case DECREASE:
                    review.decreaseLikeCount();
                    break;
                default:
                    log.info("누적 좋아요 수 변경에 실패했습니다.");
            }
            log.info("getLikeCount={} LikeAction={}", review.getLikeCount(), event.action());
            reviewRepository.save(review); // 변경된 Review 엔티티 저장
        } catch (Exception e) {
            log.error("좋아요 수 업데이트 중 오류 발생", e);
        }
        log.info("이벤트 완료: reviewId={} LikeAction={}", event.reviewId(), event.action());
    }
}
