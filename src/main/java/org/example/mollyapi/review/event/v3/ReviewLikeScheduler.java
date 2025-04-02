package org.example.mollyapi.review.event.v3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.review.entity.Review;
import org.example.mollyapi.review.event.v3.service.ReviewUpdateService;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static org.example.mollyapi.review.event.v3.key.RedisKey.REVIEW_LIKE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewLikeScheduler {
    private final StringRedisTemplate redisTemplate;
    private final ReviewUpdateService reviewUpdateService;

    /**
     * 5초마다 좋아요 처리된 데이터 100개씩 Redis -> RDB 로 Update 요청
     * */
//    @Async
//    @Scheduled(fixedDelay = 5000)
//    public void scheduler_v1(){
//        ScanOptions scanOptions = ScanOptions.scanOptions().match("reviewLike*").count(100).build();
//        Cursor<String> cursor = redisTemplate.scan(scanOptions);
//
//        if (!cursor.hasNext()) log.info("Redis에 reviewLike* 패턴을 가진 데이터가 없습니다.");
//        log.info("데이터 반영을 시작합니다..");
//
//        while (cursor.hasNext()){
//            log.info("데이터 삽입을 진행 중입니다..");
//
//            String key = cursor.next();
//            Long reviewId = extractReviewId(key);
//            if (reviewId == null) continue;
//
//            Long updateLikeCount = Long.parseLong(Objects.requireNonNull(redisTemplate.opsForValue().get(key)));
//            Review saveReview = reviewUpdateService.save(reviewId, updateLikeCount);
//            if(saveReview != null) redisTemplate.delete(key); //좋아요 update 후 Redis key 삭제
//        }
//
//        log.info("데이터 반영을 완료했습니다..");
//    }

    /**
     * Redis에서 Review Id 추출
     * @param key Redis key
     * @return reviewId 리뷰 PK
     * */
    private Long extractReviewId(String key) {
        return key.contains(REVIEW_LIKE.getValue()) ? Long.parseLong(key.substring(REVIEW_LIKE.getValue().length())) : null;
    }

}
