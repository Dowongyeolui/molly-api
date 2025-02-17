package org.example.mollyapi.review.repository;

import org.example.mollyapi.review.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
}
