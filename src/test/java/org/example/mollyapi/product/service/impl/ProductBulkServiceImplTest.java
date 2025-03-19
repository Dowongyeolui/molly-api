package org.example.mollyapi.product.service.impl;

import org.example.mollyapi.common.config.ApiQueryCounter;
import org.example.mollyapi.common.config.ApiQueryInspector;
import org.example.mollyapi.product.entity.Product;
import org.example.mollyapi.product.entity.ProductItem;
import org.example.mollyapi.product.repository.ProductItemRepository;
import org.example.mollyapi.product.repository.ProductRepository;
import org.example.mollyapi.product.service.ProductBulkService;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.example.mollyapi.user.type.Sex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;


@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ProductBulkServiceImplTest {

    @Autowired
    private ApiQueryInspector queryInspector;

    @Autowired
    private ApiQueryCounter apiQueryCounter;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductItemRepository productItemRepository;

    @Autowired
    private ProductBulkService productBulkService;



    @Test
    @DisplayName("Excel 파일로 상품등록이 가능하다.")
    void saveBulkProduct_By_Using_Excel_Success() throws IOException {

        //given
        User givenUser = userRepository.save(createUser());
        ClassPathResource resource = new ClassPathResource("test_product.xlsx");
        assertThat(resource.exists()).isTrue();

        InputStream inputStream = resource.getInputStream();
        MultipartFile file = new MockMultipartFile(
                "file",
                resource.getFilename(),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                inputStream
        );

        //when
        productBulkService.saveBulkProducts(file, givenUser.getUserId());
        List<Product> allProduct = productRepository.findAll();
        List<ProductItem> allProductItem = productItemRepository.findAll();

        //then
        assertThat(allProduct)
                .extracting("productName", "price", "brandName")
                .contains(
                        tuple("Final law", 232399L, "A Bathing Ape"),
                        tuple("Wind wife hold right", 193546L, "Paul Smith"),
                        tuple("Mean after activity case", 97226L, "Club Monaco")
                );

        assertThat(allProductItem)
                .extracting("color", "colorCode", "quantity", "size")
                .contains(
                        tuple("블루", "#FFC0CB", 59L, "S"),
                        tuple("브라운", "#FFC0CB", 362L, "S"),
                        tuple("화이트", "#000000", 156L, "XXXL")
                );


    }

    @Test
    @DisplayName("유효하지 않은 상품은 반환되고, 유효한 상품만 저장된다.")
    void saveBulkProduct_By_Using_Wrong_Excel() throws IOException {

        //given
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        User givenUser = userRepository.save(createUser());
        ClassPathResource resource = new ClassPathResource("test_wrong_product.xlsx");
        assertThat(resource.exists()).isTrue();


        InputStream inputStream = resource.getInputStream();
        MultipartFile file = new MockMultipartFile(
                "file",
                resource.getFilename(),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                inputStream
        );

        //when
        List<Map<String, String>> invalidProduct = productBulkService.saveBulkProducts(file, givenUser.getUserId());
        List<Product> allProduct = productRepository.findAll();
        List<ProductItem> allProductItem = productItemRepository.findAll();

        //then
        assertThat(allProduct)
                .extracting("productName", "price", "brandName")
                .contains(
                        tuple("Pressure majority might year", 285206L, "Champion"),
                        tuple("Safe short", 238979L, "Timberland"),
                        tuple("Hit half pass", 246424L, "Ralph Lauren")
                );

        assertThat(allProductItem)
                .extracting("color", "colorCode", "quantity", "size")
                .contains(
                        tuple("그린", "#FFFFFF", 260L, "M"),
                        tuple("그레이", "#FFC0CB", 457L, "S"),
                        tuple("블랙", "#FFFF00", 365L, "M")
                );

        assertThat(invalidProduct)
                .extracting(map -> map.get("행"))
                .contains("4", "7", "16", "101");


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
}