package org.example.mollyapi.product.repository;

import org.example.mollyapi.product.dto.request.ProductBulkItemReqDto;
import org.example.mollyapi.product.dto.request.ProductBulkReqDto;
import org.example.mollyapi.product.entity.Product;
import org.example.mollyapi.product.mapper.ProductItemMapper;
import org.example.mollyapi.product.mapper.ProductMapper;
import org.example.mollyapi.product.repository.jdbc.ProductBulkJdbcRepository;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.example.mollyapi.user.type.Sex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
class ProductBulkJdbcRepositoryTest {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductItemMapper productItemMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("배치쿼리")
    void saveProductByBatchQuery() {

        //given
        User user = userRepository.save(createUser());
        ProductBulkReqDto a = createProductBulkReqDto("A", 2L);
        ProductBulkReqDto b = createProductBulkReqDto("B", 3L);

        // 오류 발생
        ProductBulkReqDto c = createProductBulkReqDto("C", 23L);
        ProductBulkReqDto d = createProductBulkReqDto("D", 46L);

        ProductBulkItemReqDto aItem = createProductBulkItemReqDto(a.getId(), "블랙", "#000000", "M");
        ProductBulkItemReqDto bItem = createProductBulkItemReqDto(b.getId(), "흰색", "#FFFFF", "L");
        ProductBulkItemReqDto cItem = createProductBulkItemReqDto(c.getId(), "파랑", "#002300", "XL");
        ProductBulkItemReqDto dItem = createProductBulkItemReqDto(d.getId(), "분홍", "#001100", "S");

        List<ProductBulkReqDto> productBulkReqDtos = List.of(a, b, c, d);

        productMapper.insertProducts();

        List<Product> products = productRepository.findAll();


    }

    @Test
    void saveProductItemByBatchQuery() {

    }

    private User createUser() {
        return User.builder()
            .sex(Sex.FEMALE)
            .nickname("꽃달린감나무")
            .cellPhone("01011112222")
            .birth(LocalDate.now())
            .profileImage("default.jpg")
            .name("꽃감이")
            .build();
    }

    private static ProductBulkReqDto createProductBulkReqDto(String productName, long categoryId) {
        return ProductBulkReqDto.builder()
            .productName(productName)
            .brandName("방밥다망함")
            .price(12834L)
            .description("ABCDERQWSOFKSLEMFMSLALSASKFJMELFG:A")
            .categoryId(categoryId)
            .build();
    }

    private static ProductBulkItemReqDto createProductBulkItemReqDto(Long productId, String color,
        String colorCold, String size) {
        return ProductBulkItemReqDto.builder()
            .productId(productId)
            .color(color)
            .colorCode(colorCold)
            .size(size)
            .build();
    }

}

//List<ProductBulkReqDto> productBulkReqDtos, Long userId