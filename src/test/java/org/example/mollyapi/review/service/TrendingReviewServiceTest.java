package org.example.mollyapi.review.service;

import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.order.entity.OrderDetail;
import org.example.mollyapi.product.entity.Product;
import org.example.mollyapi.product.repository.ProductRepository;
import org.example.mollyapi.review.dto.response.TrendingReviewResDto;
import org.example.mollyapi.review.entity.Review;
import org.example.mollyapi.review.entity.ReviewLike;
import org.example.mollyapi.review.repository.ReviewLikeRepository;
import org.example.mollyapi.review.repository.ReviewRepository;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.example.mollyapi.user.type.Sex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.mollyapi.common.exception.error.impl.ReviewError.NOT_FOUND_REVIEW;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class TrendingReviewServiceTest {
    @Autowired
    private TrendingReviewService trendingReviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewLikeRepository reviewLikeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;
    
    
    @DisplayName("최근 7일간 좋아요가 높은 인기 순위 리뷰 Top12를 조회한다.")
    @Test
    void shouldFindTop12ReviewWhen() {
        // given
        User testUser1 = createAndSaveUser("망고", "김망고");
        User testUser2 = createAndSaveUser("사과", "이사과");
        User testUser3 = createAndSaveUser("배", "최배");
        User testUser4 = createAndSaveUser("포도", "박포도");
        Product testProduct = createAndSaveProduct();
        Review testReview1 = createAndSaveReview(testUser1, testProduct, "Test content 1");
        Review testReview2 = createAndSaveReview(testUser2, testProduct, "Test content 2");

        createAndSaveReviewLike(true, testUser1, testReview1);
        createAndSaveReviewLike(true, testUser1, testReview2);
        createAndSaveReviewLike(true, testUser2, testReview1);
        createAndSaveReviewLike(true, testUser3, testReview1);
        createAndSaveReviewLike(true, testUser3, testReview2);
        createAndSaveReviewLike(true, testUser4, testReview1);

        // when
        List<TrendingReviewResDto> trendingReviewList = reviewRepository.getTrendingReviewInfo();

        // then
        assertThat(trendingReviewList).isNotNull();
        assertThat(trendingReviewList)
                .hasSize(2)
                .extracting(TrendingReviewResDto::reviewId, TrendingReviewResDto::count, TrendingReviewResDto::nickname)
                .containsExactly(
                        tuple(testReview1.getId(), 4L, testUser1.getNickname()),
                        tuple(testReview2.getId(), 2L, testUser2.getNickname())
                );
    }

    @DisplayName("조건에 맞는 리뷰가 존재하지 않으면 예외가 발생한다.")
    @Test
    void shouldThrowExceptionWhenReviewNotFound() {
        // given & when & then
        assertThatThrownBy(() -> trendingReviewService.getTrendingReview())
                .isInstanceOf(CustomException.class)
                .hasMessage(NOT_FOUND_REVIEW.getMessage());
    }

    private User createAndSaveUser(String nickname, String name) {
        return userRepository.save(User.builder()
                .sex(Sex.FEMALE)
                .nickname(nickname)
                .cellPhone("01011112222")
                .birth(LocalDate.of(2000, 1, 2))
                .profileImage("default.jpg")
                .name(name)
                .build());
    }

    private Product createAndSaveProduct() {
        return productRepository.save(Product.builder()
                .productName("테스트 상품")
                .brandName("테스트 브랜드")
                .price(50000L)
                .build());
    }

    private Review createAndSaveReview(User user, Product product, String content) {
        return reviewRepository.save(Review.builder()
                .content(content)
                .isDeleted(false)
                .count(0L)
                .user(user)
                .orderDetail(null)
                .product(product)
                .build());
    }

    private void createAndSaveReviewLike(boolean status, User user, Review review) {
        reviewLikeRepository.save(ReviewLike.builder()
                .isLike(status)
                .user(user)
                .review(review)
                .build());
    }
}
