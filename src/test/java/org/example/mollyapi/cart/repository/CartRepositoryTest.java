package org.example.mollyapi.cart.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.example.mollyapi.MollyApiApplication;
import org.example.mollyapi.cart.dto.Response.CartInfoDto;
import org.example.mollyapi.cart.entity.Cart;
import org.example.mollyapi.product.dto.UploadFile;
import org.example.mollyapi.product.entity.Product;
import org.example.mollyapi.product.entity.ProductImage;
import org.example.mollyapi.product.entity.ProductItem;
import org.example.mollyapi.product.repository.ProductImageRepository;
import org.example.mollyapi.product.repository.ProductItemRepository;
import org.example.mollyapi.product.repository.ProductRepository;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.example.mollyapi.user.type.Sex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class CartRepositoryTest {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductItemRepository productItemRepository;

    @Autowired
    EntityManager entityManager;
    private JPAQueryFactory queryFactory;

    private User testUser;
    private Product testProduct;
    private ProductImage testImage;
    private ProductItem testItem;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testUser = createAndSaveUser();
        testProduct = createAndSaveProduct();
        testImage = createAndSaveProductImage(testProduct);
        testItem = createAndSaveProductItem("S", testProduct);
        testCart = createAndSaveCart(2L, testUser, testItem);
    }

    @DisplayName("장바구니에 동일한 상품이 담겨있는 않은 지 조회한다.")
    @Test
    void VerifyProductItemNotExistInCart() {
        //given
        Long userId = 1L;
        Long itemId = testItem.getId();

        //when
        Cart cart = cartRepository.findByProductItemIdAndUserUserId(itemId, userId);

        //then
        assertThat(cart).isNull();
    }

    @DisplayName("장바구니에 동일한 상품이 담겨있는 지를 조회한다.")
    @Test
    void VerifyProductItemExistInCart() {
        //given
        Long userId = testUser.getUserId();
        Long itemId = testItem.getId();

        //when
        Cart cart = cartRepository.findByProductItemIdAndUserUserId(itemId, userId);

        //then
        assertThat(cart).isNotNull();
        assertThat(cart.getProductItem()).isEqualTo(testItem);
        assertThat(cart.getUser()).isEqualTo(testUser);
    }

    @ParameterizedTest
    @CsvSource({"1, true", "2, false"})
    @DisplayName("장바구니에 담긴 수량을 조회할 때, 장바구니 최대수량에 따라 상태값을 반환한다.")
    void checkMaxCart(Long userId, boolean status) {
        //given //when
        boolean isMaxCount = cartRepository.countByUserUserId(userId);

        //then
        assertEquals(isMaxCount, status);
    }

    @DisplayName("존재하는 장바구니 내역 상세를 조회한다.")
    @Test
    void findByCartIdAndUserId_Success() {
        //given
        Long userId = testUser.getUserId();
        Long cartId = testCart.getCartId();

        //when
        Optional<Cart> cart = cartRepository.findByCartIdAndUserUserId(cartId, userId);

        //then
        assertThat(cart).isPresent();
        assertThat(cart.get().getUser()).isEqualTo(testUser);
        assertThat(cart.get().getCartId()).isEqualTo(cartId);
    }

    @DisplayName("존재하지 않는 장바구니 내역 상세를 조회한다.")
    @Test
    void findByCartIdAndUserId_NotFound() {
        //given
        Long userId = 4L;
        Long cartId = 999L;

        //when
        Optional<Cart> cart = cartRepository.findByCartIdAndUserUserId(cartId, userId);

        //then
        assertThat(cart).isEmpty();
    }

    @DisplayName("사용자의 장바구니 전체 내역을 조회한다.")
    @Test
    void findAllCartInfoByUserId() {
        //given
        Long userId = testUser.getUserId();
        ProductItem secondItem = createAndSaveProductItem("L", testProduct);
        Cart secondCart = createAndSaveCart(3L, testUser, secondItem);

        //when
        List<CartInfoDto> cartInfoList = cartRepository.getCartInfo(userId);

        //then
        assertThat(cartInfoList).hasSize(2);
        assertThat(cartInfoList)
                .extracting(CartInfoDto::cartId, CartInfoDto::itemId, CartInfoDto::quantity)
                .contains(
                        tuple(secondCart.getCartId(), secondItem.getId(), secondCart.getQuantity()),
                        tuple(testCart.getCartId(), testCart.getProductItem().getId(), testCart.getQuantity())
                );
    }

    private User createAndSaveUser() {
        return userRepository.save(User.builder()
                .sex(Sex.FEMALE)
                .nickname("망고")
                .cellPhone("01011112222")
                .birth(LocalDate.of(2000, 1, 2))
                .profileImage("default.jpg")
                .name("김망고")
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

    private ProductImage createAndSaveProductImage(Product product) {
        return productImageRepository.save(ProductImage.builder()
                .uploadFile(UploadFile.builder()
                        .storedFileName("/images/product/coolfit_bra_volumefit_1.jpg")
                        .uploadFileName("coolfit_bra_volumefit_1.jpg")
                        .build())
                .isRepresentative(true)
                .imageIndex(0L)
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
}
