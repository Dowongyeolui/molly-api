package org.example.mollyapi.review.repository;

import org.example.mollyapi.review.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    void deleteAllByReviewId(Long reviewId);
}
