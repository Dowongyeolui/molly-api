package org.example.mollyapi.review.repository;

import org.example.mollyapi.cart.entity.Cart;
import org.example.mollyapi.cart.repository.CartRepository;
import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.order.entity.OrderDetail;
import org.example.mollyapi.order.repository.OrderDetailRepository;
import org.example.mollyapi.order.repository.OrderRepository;
import org.example.mollyapi.product.entity.Product;
import org.example.mollyapi.product.entity.ProductItem;
import org.example.mollyapi.product.repository.ProductItemRepository;
import org.example.mollyapi.product.repository.ProductRepository;
import org.example.mollyapi.review.dto.request.UpdateReviewLikeReqDto;
import org.example.mollyapi.review.entity.Review;
import org.example.mollyapi.review.entity.ReviewLike;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.example.mollyapi.user.type.Sex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.example.mollyapi.order.type.CancelStatus.NONE;
import static org.example.mollyapi.order.type.OrderStatus.SUCCEEDED;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class ReviewLikeRepositoryTest {
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewLikeRepository reviewLikeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductItemRepository productItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @DisplayName("특정 리뷰에 대한 사용자의 좋아요 여부를 조회할 때, 좋아요가 존재하는 경우")
    @Test
    void findReviewLikeWhenLikeExist() {
        // given
        User testUser = createAndSaveUser("망고", "김망고");
        Product testProduct = createAndSaveProduct();
        ProductItem testItem = createAndSaveProductItem("S", testProduct);
        Cart testCart = createAndSaveCart(3L, testUser, testItem);
        Order testOrder = createAndSaveOrder(testUser);
        OrderDetail testOrderDetail = createAndSaveOrderDetail(testOrder, testItem, testCart.getQuantity(), testCart.getCartId());
        Review testReview = createAndSaveReview(testUser, testOrderDetail, testProduct, "test 1");

        Long userId = testUser.getUserId();
        Long reviewId = testReview.getId();
        ReviewLike testReviewLike = createAndSaveReviewLike(true, testUser, testReview);
        UpdateReviewLikeReqDto likeDto = new UpdateReviewLikeReqDto(reviewId, true);

        // when
        ReviewLike reviewLike = reviewLikeRepository.findByReviewIdAndUserUserId(likeDto.reviewId(), userId);

        // then
        assertThat(reviewLike).isNotNull();
        assertThat(reviewLike.getIsLike()).isEqualTo(testReviewLike.getIsLike());
    }

    @DisplayName("특정 리뷰에 대한 사용자의 좋아요 여부를 조회할 때, 좋아요가 존재하지 않는 경우")
    @Test
    void findReviewLikeWhenLikeNotExist() {
        // given
        User testUser = createAndSaveUser("망고", "김망고");
        Product testProduct = createAndSaveProduct();
        ProductItem testItem = createAndSaveProductItem("S", testProduct);
        Cart testCart = createAndSaveCart(3L, testUser, testItem);
        Order testOrder = createAndSaveOrder(testUser);
        OrderDetail testOrderDetail = createAndSaveOrderDetail(testOrder, testItem, testCart.getQuantity(), testCart.getCartId());
        Review testReview = createAndSaveReview(testUser, testOrderDetail, testProduct, "test 1");
        UpdateReviewLikeReqDto likeDto = new UpdateReviewLikeReqDto(testReview.getId(), true);

        // when
        ReviewLike reviewLike = reviewLikeRepository.findByReviewIdAndUserUserId(likeDto.reviewId(), testUser.getUserId());

        // then
        assertThat(reviewLike).isNull();
    }

    @DisplayName("특정 리뷰에 해당하는 모든 좋아요를 삭제한다")
    @Test
    void deleteAllLikeByReview() {
        // given
        User testUser = createAndSaveUser("망고", "김망고");
        User testUser2 = createAndSaveUser("사과", "이사과");
        Product testProduct = createAndSaveProduct();
        ProductItem testItem = createAndSaveProductItem("S", testProduct);
        Cart testCart = createAndSaveCart(3L, testUser, testItem);
        Order testOrder = createAndSaveOrder(testUser);
        OrderDetail testOrderDetail = createAndSaveOrderDetail(testOrder, testItem, testCart.getQuantity(), testCart.getCartId());
        Review testReview = createAndSaveReview(testUser, testOrderDetail, testProduct, "test 1");

        ReviewLike testReviewLike1 = createAndSaveReviewLike(true, testUser, testReview);
        ReviewLike testReviewLike2 = createAndSaveReviewLike(true, testUser2, testReview);

        // when
        reviewLikeRepository.deleteAllByReviewId(testReview.getId());

        // then
        assertThat(reviewLikeRepository.existsById(testReviewLike1.getId())).isFalse();
        assertThat(reviewLikeRepository.existsById(testReviewLike2.getId())).isFalse();
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

    private Order createAndSaveOrder(User user) {
        return orderRepository.save(Order.builder()
                .tossOrderId("ORD-20250309021413-5931")
                .user(user)
                .totalAmount(10000L)
                .status(SUCCEEDED)
                .cancelStatus(NONE)
                .orderedAt(LocalDateTime.of(2025, 3, 9, 11,10))
                .expirationTime(LocalDateTime.of(2025, 3, 9, 11, 10))
                .build());
    }

    private OrderDetail createAndSaveOrderDetail(Order order, ProductItem item, Long quantity, Long cartId) {
        return orderDetailRepository.save(OrderDetail.builder()
                .order(order)
                .productItem(item)
                .size(item.getSize())
                .price(item.getProduct().getPrice())
                .quantity(quantity)
                .brandName(item.getProduct().getBrandName())
                .productName(item.getProduct().getProductName())
                .cartId(cartId)
                .build());
    }

    private Product createAndSaveProduct() {
        return productRepository.save(Product.builder()
                .productName("테스트 상품")
                .brandName("테스트 브랜드")
                .price(50000L)
                .build());
    }

    private ProductItem createAndSaveProductItem(String size, Product product) {
        return productItemRepository.save(ProductItem.builder()
                .color("WHITE")
                .colorCode("#FFFFFF")
                .size(size)
                .quantity(30L)
                .product(product)
                .build());
    }

    private Cart createAndSaveCart(Long quantity, User user, ProductItem productItem) {
        return cartRepository.save(Cart.builder()
                .quantity(quantity)
                .user(user)
                .productItem(productItem)
                .build());
    }

    private Review createAndSaveReview(User user, OrderDetail orderDetail, Product product, String content) {
        return reviewRepository.save(Review.builder()
                .content(content)
                .isDeleted(false)
                .count(0L)
                .user(user)
                .orderDetail(orderDetail)
                .product(product)
                .build());
    }

    private ReviewLike createAndSaveReviewLike(boolean status, User user, Review review) {
        return reviewLikeRepository.save(ReviewLike.builder()
                .isLike(status)
                .user(user)
                .review(review)
                .build());
    }
}
