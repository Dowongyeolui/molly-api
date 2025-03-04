package org.example.mollyapi.review.repository;

import org.example.mollyapi.review.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    void deleteAllByReviewId(Long reviewId);
    List<ReviewImage> findAllByReviewId(Long reviewId);
    @Transactional
    void deleteAllByReviewIdIn(List<Long> reviewIds);
}
