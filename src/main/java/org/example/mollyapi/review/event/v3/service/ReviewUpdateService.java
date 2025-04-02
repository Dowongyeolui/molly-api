package org.example.mollyapi.review.event.v3.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.review.entity.Review;
import org.example.mollyapi.review.repository.ReviewRepository;
import org.example.mollyapi.review.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewUpdateService {
    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;

//    @Autowired
//    private JdbcTemplate jdbcTemplate;

    @Transactional
    public Review save(Long reviewId, Long likeCount) {
        Review review = reviewService.findByReview(reviewId);
        review.updateLikeCount(likeCount);
        log.info("reviewId={} likeCount={}", reviewId, likeCount);
        return reviewRepository.save(review);
    }

//    @Transactional
//    public void batchUpdateReviews(List<Review> reviews) {
//        jdbcTemplate.batchUpdate("UPDATE review SET like_count = ? WHERE review_id = ?",
//                reviews.stream()
//                        .map(review -> new Object[]{review.getLikeCount(), review.getId()})
//                        .collect(Collectors.toList())
//        );
//    }

}
