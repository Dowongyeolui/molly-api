package org.example.mollyapi.order.service;

import org.example.mollyapi.cart.repository.CartRepository;
import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.order.repository.OrderDetailRepository;
import org.example.mollyapi.order.repository.OrderRepository;
import org.example.mollyapi.order.type.OrderStatus;
import org.example.mollyapi.payment.dto.response.TossConfirmResDto;
import org.example.mollyapi.product.dto.UploadFile;
import org.example.mollyapi.product.entity.Product;
import org.example.mollyapi.product.entity.ProductImage;
import org.example.mollyapi.product.entity.ProductItem;
import org.example.mollyapi.product.repository.ProductImageRepository;
import org.example.mollyapi.product.repository.ProductItemRepository;
import org.example.mollyapi.product.repository.ProductRepository;
import org.example.mollyapi.product.service.ProductServiceImpl;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.example.mollyapi.user.type.Sex;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderCancelServiceTest {

    @Autowired
    OrderServiceImpl orderService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderDetailRepository orderDetailRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductItemRepository productItemRepository;

    @Autowired
    ProductServiceImpl productService;

    @Autowired
    ProductImageRepository productImageRepository;

    private User savedUser;
    private Product savedProduct;
    private ProductItem savedProductItem;
    private Order savedOrder;
    String tossOrderId = "ORD-20250213132349-6572";
    String paymentKey = "PAY-20250213132349-6572";

    @BeforeEach
    void setup(){
        User user = User.builder()
                .name("test_user")
                .cellPhone("01012345678")
                .flag(true)
                .nickname("test_nickname")
                .sex(Sex.FEMALE)
                .point(1000)
                .build();
        savedUser = userRepository.save(user);

        Product product = Product.builder()
                .user(savedUser)
                .brandName("adidas")
                .price(5000L)
                .build();
        savedProduct = productRepository.save(product);

        MockMultipartFile mockfile = new MockMultipartFile(
                "file",
                "test-file.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{1, 2, 3, 4}
        );
        productService.registerProductImages(savedProduct, List.of(mockfile));

        // 대표 이미지 강제 추가
        ProductImage representativeImage = ProductImage.createThumbnail(savedProduct,
                UploadFile.builder()
                        .storedFileName("/images/product/test-thumbnail.png")
                        .uploadFileName("test-thumbnail.png")
                        .build()
        );
        savedProduct.addImage(representativeImage);
        savedProduct = productRepository.save(savedProduct);

        ProductItem productItem = ProductItem.builder()
                .color("blue")
                .size("M")
                .quantity(5L) // 재고 5개
                .product(savedProduct)
                .build();
        savedProductItem = productItemRepository.save(productItem);

        savedOrder = orderRepository.save(new Order(savedUser, "ORD-202503111234-5678"));
        savedOrder.updateStatus(OrderStatus.PENDING);
        orderRepository.save(savedOrder);

        // Mock paymentWebClientUtil
        TossConfirmResDto mockResponse = new TossConfirmResDto(
                null,  // mId
                null,  // version
                "tORD-20250213132349-6572",  // paymentKey
                "SUCCESS",  // status
                null,  // lastTransactionKey
                null,  // method
                tossOrderId,  // orderId
                null,  // orderName
                10000L,  // totalAmount
                null,  // card (nullable)
                null   // easyPay (nullable)
        );
    }

    @AfterEach
    void cleanUpDatabase() {
        cartRepository.deleteAllInBatch();
        orderDetailRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        productImageRepository.deleteAllInBatch();
        productItemRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("주문이 PENDING 상태일 경우 정상적으로 취소된다.")
    void cancelOrder_Success() {
        /// given -> @BeforeEach에서 이미 PENDING 상태로 저장됨

        /// when
        String result = orderService.cancelOrder(savedOrder.getId());

        /// then
        assertThat(result).isEqualTo("주문이 취소되었습니다.");
        assertThat(orderRepository.findById(savedOrder.getId())).isEmpty(); // 삭제 확인
    }

    @Test
    @DisplayName("주문 조회 실패 - 존재하지 않는 주문 ID로 취소를 요청하면 예외가 발생")
    void cancelOrder_OrderNotFound_ShouldThrowException() {
        /// given
        Long invalidOrderId = 999L;

        /// when & then
        assertThatThrownBy(() -> orderService.cancelOrder(invalidOrderId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 주문을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("PENDING 상태가 아닌 주문은 취소할 수 없다.")
    void cancelOrder_NonPendingStatus_ShouldThrowException() {
        /// given
        savedOrder.updateStatus(OrderStatus.SUCCEEDED); // 이미 결제된 상태
        orderRepository.save(savedOrder);

        /// when & then
        assertThatThrownBy(() -> orderService.cancelOrder(savedOrder.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제 요청이 진행된 주문은 취소할 수 없습니다.");
    }
}