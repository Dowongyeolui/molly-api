package org.example.mollyapi.review.repository;

import org.example.mollyapi.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewCustomRepository {
    Review findByIsDeletedAndOrderDetailIdAndUserUserId(Boolean isDeleted, Long orderDetail, Long userId);
}
