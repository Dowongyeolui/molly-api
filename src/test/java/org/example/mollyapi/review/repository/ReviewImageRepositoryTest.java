package org.example.mollyapi.review.repository;

import org.example.mollyapi.cart.entity.Cart;
import org.example.mollyapi.cart.repository.CartRepository;
import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.order.entity.OrderDetail;
import org.example.mollyapi.order.repository.OrderDetailRepository;
import org.example.mollyapi.order.repository.OrderRepository;
import org.example.mollyapi.product.dto.UploadFile;
import org.example.mollyapi.product.entity.Product;
import org.example.mollyapi.product.entity.ProductItem;
import org.example.mollyapi.product.repository.ProductItemRepository;
import org.example.mollyapi.product.repository.ProductRepository;
import org.example.mollyapi.review.entity.Review;
import org.example.mollyapi.review.entity.ReviewImage;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.tuple;
import static org.example.mollyapi.order.type.CancelStatus.NONE;
import static org.example.mollyapi.order.type.OrderStatus.SUCCEEDED;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class ReviewImageRepositoryTest {
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewImageRepository reviewImageRepository;

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

    @DisplayName("특정 리뷰에 해당하는 모든 이미지들을 조회한다.")
    @Test
    void findAllImageByReview() {
        // given
        User testUser = createAndSaveUser("망고", "김망고");
        Product testProduct = createAndSaveProduct();
        ProductItem testItem = createAndSaveProductItem("S", testProduct);
        Cart testCart = createAndSaveCart(3L, testUser, testItem);
        Order testOrder = createAndSaveOrder(testUser);
        OrderDetail testOrderDetail = createAndSaveOrderDetail(testOrder, testItem, testCart.getQuantity(), testCart.getCartId());
        Review testReview = createAndSaveReview(testUser, testOrderDetail, testProduct, "test 1");
        ReviewImage testImage1 = createAndSaveReviewImage(testReview, 0L, UploadFile.builder()
                .storedFileName("/images/review/test_1.jpg")
                .uploadFileName("test_1.jpg")
                .build());
        ReviewImage testImage2 = createAndSaveReviewImage(testReview, 1L, UploadFile.builder()
                .storedFileName("/images/review/test_2.jpg")
                .uploadFileName("test_2.jpg")
                .build());

        // when
        List<ReviewImage> reviewImageList = reviewImageRepository.findAllByReviewId(testReview.getId());

        // then
        assertThat(reviewImageList).hasSize(2);
        assertThat(reviewImageList)
                .extracting(ReviewImage::getFilename, ReviewImage::getImageIndex)
                .contains(
                        tuple(testImage1.getFilename(), testImage1.getImageIndex()),
                        tuple(testImage2.getFilename(), testImage2.getImageIndex())
                );
    }

    @DisplayName("특정 리뷰에 해당하는 모든 이미지들을 삭제한다.")
    @Test
    void deleteAllImageByReview() {
        // given
        User testUser = createAndSaveUser("망고", "김망고");
        Product testProduct = createAndSaveProduct();
        ProductItem testItem = createAndSaveProductItem("S", testProduct);
        Cart testCart = createAndSaveCart(3L, testUser, testItem);
        Order testOrder = createAndSaveOrder(testUser);
        OrderDetail testOrderDetail = createAndSaveOrderDetail(testOrder, testItem, testCart.getQuantity(), testCart.getCartId());
        Review testReview = createAndSaveReview(testUser, testOrderDetail, testProduct, "test 1");
        ReviewImage testImage1 = createAndSaveReviewImage(testReview, 0L, UploadFile.builder()
                .storedFileName("/images/review/test_1.jpg")
                .uploadFileName("test_1.jpg")
                .build());
        ReviewImage testImage2 = createAndSaveReviewImage(testReview, 1L, UploadFile.builder()
                .storedFileName("/images/review/test_2.jpg")
                .uploadFileName("test_2.jpg")
                .build());

        // when
        reviewImageRepository.deleteAllByReviewId(testReview.getId());

        // then
        assertThat(reviewImageRepository.existsById(testImage1.getId())).isFalse();
        assertThat(reviewImageRepository.existsById(testImage2.getId())).isFalse();
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

    private ReviewImage createAndSaveReviewImage(Review review, Long idx, UploadFile uploadFile) {
        return reviewImageRepository.save(ReviewImage.builder()
                .uploadFile(uploadFile)
                .imageIndex(idx)
                .isVideo(false)
                .review(review)
                .build());
    }
}
