package org.example.mollyapi.review.event.v4;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.review.entity.Review;
import org.example.mollyapi.review.event.v4.service.ReviewUpdateService_v4;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

import static org.example.mollyapi.review.event.v3.key.RedisKey.REVIEW_LIKE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewLikeScheduler_v4 {
    private final StringRedisTemplate redisTemplate;
    private final ReviewUpdateService_v4 reviewUpdateService;

    /**
     * 5초마다 좋아요 처리된 데이터 100개씩 Redis -> RDB 로 Update 요청
     * */
    @Async
    @Scheduled(fixedDelay = 5000)
    public void scheduler_v4(){
        log.info("좋아요 데이터 반영 시작..");
        ScanOptions scanOptions = ScanOptions.scanOptions().match("reviewLike*").count(100).build();
        Cursor<String> cursor = redisTemplate.scan(scanOptions);

        while (cursor.hasNext()) {
            String key = cursor.next();
            Long reviewId = extractReviewId(key);
            if (reviewId == null) continue;

            String lockKey = "lock:reviewLike:" + reviewId;  // 분산 락 키 생성
            String lockValue = UUID.randomUUID().toString(); // 락 값 설정
            Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, Duration.ofSeconds(5));

            if (Boolean.TRUE.equals(lockAcquired)) {  // 락 획득 성공 시
                try {
                    Long updateLikeCount = Long.parseLong(Objects.requireNonNull(redisTemplate.opsForValue().get(key)));
                    Review saveReview = reviewUpdateService.save(reviewId, updateLikeCount);
                    if (saveReview != null) redisTemplate.delete(key); //좋아요 update 후 Redis key 삭제
                } catch (NumberFormatException e) {
                    log.error("잘못된 형식의 값 감지 (key={}): {}", key, redisTemplate.opsForValue().get(key));
                } finally {
                    // 락 해제 (해당 락을 본인이 소유한 경우만 삭제)
                    String currentLockValue = redisTemplate.opsForValue().get(lockKey);
                    if (lockValue.equals(currentLockValue)) {
                        redisTemplate.delete(lockKey);
                        log.info("락 해제 완료={}", lockKey);
                    }
                }
            } else {
                log.warn("다른 프로세스에서 reviewId={} 처리 중, 스킵", reviewId);
            }
        }
        log.info("데이터 반영을 완료했습니다..");
    }

    /**
     * Redis에서 Review Id 추출
     * @param key Redis key
     * @return reviewId 리뷰 PK
     * */
    private Long extractReviewId(String key) {
        return key.contains(REVIEW_LIKE.getValue()) ? Long.parseLong(key.substring(REVIEW_LIKE.getValue().length())) : null;
    }
}
