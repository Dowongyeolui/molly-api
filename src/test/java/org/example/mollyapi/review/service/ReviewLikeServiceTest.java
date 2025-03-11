package org.example.mollyapi.review.service;

import org.example.mollyapi.cart.entity.Cart;
import org.example.mollyapi.cart.repository.CartRepository;
import org.example.mollyapi.common.exception.CustomException;
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
import org.example.mollyapi.review.repository.ReviewLikeRepository;
import org.example.mollyapi.review.repository.ReviewRepository;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.example.mollyapi.user.type.Sex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.example.mollyapi.common.exception.error.impl.ReviewError.NOT_EXIST_REVIEW;
import static org.example.mollyapi.common.exception.error.impl.UserError.NOT_EXISTS_USER;
import static org.example.mollyapi.order.type.CancelStatus.NONE;
import static org.example.mollyapi.order.type.OrderStatus.SUCCEEDED;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class ReviewLikeServiceTest {
    @Autowired
    private ReviewLikeService reviewLikeService;

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

    @DisplayName("리뷰 좋아요 상태를 변경한다. (새로운 좋아요 생성)")
    @Test
    void changeReviewLike_createNewLike() {
        // given
        User testUser = createAndSaveUser("망고", "김망고");
        Product testProduct = createAndSaveProduct();
        ProductItem testItem = createAndSaveProductItem("S", testProduct);
        Cart testCart = createAndSaveCart(3L, testUser, testItem);
        Order testOrder = createAndSaveOrder(testUser);
        OrderDetail testOrderDetail = createAndSaveOrderDetail(testOrder, testItem, testCart.getQuantity(), testCart.getCartId());
        Review testReview = createAndSaveReview(testUser, testOrderDetail, testProduct, "Test content");

        UpdateReviewLikeReqDto likeReqDto = new UpdateReviewLikeReqDto(testReview.getId(), true);

        // when
        reviewLikeService.changeReviewLike(likeReqDto, testUser.getUserId());
        ReviewLike newReviewLike = reviewLikeRepository.findByReviewIdAndUserUserId(testReview.getId(), testUser.getUserId());

        // then
        assertThat(newReviewLike.getIsLike()).isTrue();
    }

    @DisplayName("리뷰 좋아요 상태를 변경한다. (이미 존재하는 좋아요 없데이트")
    @ParameterizedTest
    @CsvSource({"true", "false"})
    void changeReviewLike_updateExistReview(boolean status) {
        // given
        User testUser = createAndSaveUser("망고", "김망고");
        Product testProduct = createAndSaveProduct();
        ProductItem testItem = createAndSaveProductItem("S", testProduct);
        Cart testCart = createAndSaveCart(3L, testUser, testItem);
        Order testOrder = createAndSaveOrder(testUser);
        OrderDetail testOrderDetail = createAndSaveOrderDetail(testOrder, testItem, testCart.getQuantity(), testCart.getCartId());
        Review testReview = createAndSaveReview(testUser, testOrderDetail, testProduct, "Test content");

        UpdateReviewLikeReqDto likeReqDto = new UpdateReviewLikeReqDto(testReview.getId(), status);

        createAndSaveReviewLike(false, testUser, testReview); //좋아요 생성

        // when
        reviewLikeService.changeReviewLike(likeReqDto, testUser.getUserId());
        ReviewLike newReviewLike = reviewLikeRepository.findByReviewIdAndUserUserId(testReview.getId(), testUser.getUserId());

        // then
        assertThat(newReviewLike.getIsLike()).isEqualTo(status);
    }

    @DisplayName("존재하지 않는 사용자가 리뷰를 등록하려고 하면 예외가 발생한다.")
    @Test
    void shouldThrowExceptionWhenUserNotFoundOnChangeReviewLike() {
        // given
        User testUser = createAndSaveUser("망고", "김망고");
        Product testProduct = createAndSaveProduct();
        ProductItem testItem = createAndSaveProductItem("S", testProduct);
        Cart testCart = createAndSaveCart(3L, testUser, testItem);
        Order testOrder = createAndSaveOrder(testUser);
        OrderDetail testOrderDetail = createAndSaveOrderDetail(testOrder, testItem, testCart.getQuantity(), testCart.getCartId());
        Review testReview = createAndSaveReview(testUser, testOrderDetail, testProduct, "Test content");

        Long userId = 999L;
        UpdateReviewLikeReqDto likeReqDto = new UpdateReviewLikeReqDto(testReview.getId(), true);

        // when & then
        assertThatThrownBy(() -> reviewLikeService.changeReviewLike(likeReqDto, userId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOT_EXISTS_USER.getMessage());
    }

    @DisplayName("존재하지 않는 리뷰에 좋아요를 누르려고 하면 예외가 발생한다.")
    @Test
    void shouldThrowExceptionWhenReviewNotFoundOnChangeReviewLike() {
        // given
        User testUser = createAndSaveUser("망고", "김망고");
        Long reviewId = 999L;
        UpdateReviewLikeReqDto likeReqDto = new UpdateReviewLikeReqDto(reviewId, true);

        // when & then
        assertThatThrownBy(() -> reviewLikeService.changeReviewLike(likeReqDto, testUser.getUserId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOT_EXIST_REVIEW.getMessage());
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

    private void createAndSaveReviewLike(boolean status, User user, Review review) {
        reviewLikeRepository.save(ReviewLike.builder()
                .isLike(status)
                .user(user)
                .review(review)
                .build());
    }
}
