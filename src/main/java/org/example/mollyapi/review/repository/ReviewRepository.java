package org.example.mollyapi.review.repository;

import org.example.mollyapi.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewCustomRepository {
    Review findByIsDeletedAndOrderDetailIdAndUserUserId(Boolean isDeleted, Long orderDetail, Long userId);
    Optional<Review> findByIdAndIsDeleted(Long reviewId, Boolean isDeleted);
    Optional<Review> findByIdAndUserUserId(Long reviewId, Long userId);
}
