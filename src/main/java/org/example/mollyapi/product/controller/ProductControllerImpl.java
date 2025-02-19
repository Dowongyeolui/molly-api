package org.example.mollyapi.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.common.exception.CustomErrorResponse;
import org.example.mollyapi.product.dto.BrandSummaryDto;
import org.example.mollyapi.product.dto.ProductFilterCondition;
import org.example.mollyapi.product.dto.response.ListResDto;
import org.example.mollyapi.product.dto.response.PageResDto;
import org.example.mollyapi.product.entity.Category;
import org.example.mollyapi.product.enums.OrderBy;
import org.example.mollyapi.product.service.CategoryService;
import org.example.mollyapi.product.service.ProductService;
import org.example.mollyapi.product.dto.request.ProductReqDto;
import org.example.mollyapi.product.dto.response.ProductResDto;
import org.example.mollyapi.user.auth.annotation.Auth;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Tag(name = "Product Controller", description = "상품 관련 엔드포인트")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductControllerImpl {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "상품 정보 목록",
            description = "상품 정보와 옵션별 상품 아이템 데이터 조회,  " +
                    "파라미터 예시: ?categories=여성,아우터,  " +
                    "priceGoe= ~이상, priceLt= ~미만"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 목록 반환",
                    content = @Content(schema = @Schema(implementation = ListResDto.class))),
            @ApiResponse(responseCode = "204", description = "조회 데이터 없음", content = @Content(schema = @Schema(type = "string", example = ""))),
            @ApiResponse(responseCode = "400", description = "실패",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    public ResponseEntity<ListResDto> getAllProducts(
            @RequestParam(required = false) String colorCode,
            @RequestParam(required = false) String productSize,
            @RequestParam(required = false) String categories,
            @RequestParam(required = false) String brandName,
            @RequestParam(required = false) Long priceGoe,
            @RequestParam(required = false) Long priceLt,
            @RequestParam(required = false) OrderBy orderBy,
            @RequestParam int page,
            @RequestParam int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size);

        List<Long> categoryIdList = new ArrayList<>();
        List<String> categoriesList = categories == null ? null : Arrays.asList(categories.split(","));
        if (categoriesList != null) {
            Category category = categoryService.getCategory(categoriesList);
            categoryIdList = categoryService.getLeafCategories(category).stream().map(Category::getId).toList();
        }

        ProductFilterCondition condition = new ProductFilterCondition(
                colorCode,
                productSize,
                categoryIdList,
                brandName,
                priceGoe,
                priceLt,
                null,
                orderBy
        );

        Slice<ProductResDto> products = productService.getAllProducts(condition, pageRequest);

        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ListResDto(
                        new PageResDto(
                                (long) products.getContent().size(),
                                products.hasNext(),
                                products.isFirst(),products.isLast()
                        ),
                        products.getContent()
                        ));
    }

    @Auth
    @GetMapping("/seller")
    @Operation(summary = "상품 정보 목록(판매자용)",
            description = "상품 정보와 옵션별 상품 아이템 데이터 조회,  " +
                    "파라미터 예시: ?categories=여성,아우터,  " +
                    "priceGoe= ~이상, priceLt= ~미만"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 목록 반환",
                    content = @Content(schema = @Schema(implementation = ListResDto.class))),
            @ApiResponse(responseCode = "204", description = "조회 데이터 없음", content = @Content(schema = @Schema(type = "string", example = ""))),
            @ApiResponse(responseCode = "400", description = "실패",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    public ResponseEntity<ListResDto> getAllProductsBySeller(
            HttpServletRequest request,
            @RequestParam(required = false) String colorCode,
            @RequestParam(required = false) String productSize,
            @RequestParam(required = false) String categories,
            @RequestParam(required = false) String brandName,
            @RequestParam(required = false) Long priceGoe,
            @RequestParam(required = false) Long priceLt,
            @RequestParam(required = false) OrderBy orderBy,
            @RequestParam int page,
            @RequestParam int size
    ) {
        Long userId = (Long) request.getAttribute("userId");
        PageRequest pageRequest = PageRequest.of(page, size);

        List<Long> categoryIdList = new ArrayList<>();
        List<String> categoriesList = categories == null ? null : Arrays.asList(categories.split(","));

        if (categoriesList != null) {
            Category category = categoryService.getCategory(categoriesList);
            categoryIdList = categoryService.getLeafCategories(category).stream().map(Category::getId).toList();
        }

        ProductFilterCondition condition = new ProductFilterCondition(
                colorCode,
                productSize,
                categoryIdList,
                brandName,
                priceGoe,
                priceLt,
                userId,
                orderBy
        );

        Slice<ProductResDto> products = productService.getAllProducts(condition, pageRequest);

        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ListResDto(
                        new PageResDto(
                                (long) products.getContent().size(),
                                products.hasNext(),
                                products.isFirst(),products.isLast()
                        ),
                        products.getContent()
                ));
    }


    @GetMapping("/{productId}")
    @Operation(summary = "상품 정보 및 상품아이템 목록", description = "상품 정보와 옵션별 상품 아이템 데이터 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ProductResDto.class))),
            @ApiResponse(responseCode = "204", description = "조회 데이터 없음", content = @Content(schema = @Schema(type = "string", example = ""))),
            @ApiResponse(responseCode = "400", description = "실패",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    public ResponseEntity<ProductResDto> getProduct(@PathVariable Long productId) {
        ProductResDto productResDto = productService.getProductById(productId).orElse(null);
        if (productResDto == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productResDto);
    }

    @GetMapping("/popular-brand")
    @Operation(summary = "인기 브랜드 목록 조회", description = "브랜드별 조회수가 높은 순으로 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 목록 반환",
                    content = @Content(schema = @Schema(implementation = ListResDto.class))),
            @ApiResponse(responseCode = "204", description = "조회 데이터 없음",
                    content = @Content(schema = @Schema(type = "string", example = ""))),
            @ApiResponse(responseCode = "400", description = "실패",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    public ResponseEntity<ListResDto> getPopularBrand(
            @RequestParam int page,
            @RequestParam int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size);

        Slice<BrandSummaryDto> brands = productService.getPopularBrand(pageRequest);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ListResDto(
                        new PageResDto(
                                (long) brands.getContent().size(),
                                brands.hasNext(),
                                brands.isFirst(),brands.isLast()
                        ),
                        brands.getContent()
                ));
    }


    @Auth
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "상품 정보 및 상품아이템 등록", description = "상품 정보와 옵션별 상품 아이템 등록")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ProductResDto.class))),
            @ApiResponse(responseCode = "400", description = "실패",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    public ResponseEntity<ProductResDto> registerProduct(
            HttpServletRequest request,
            @Valid @RequestPart("product") ProductReqDto productReqDto,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "productImages", required = false) List<MultipartFile> productImages,
            @RequestPart(value = "productDescriptionImages", required = false) List<MultipartFile> productDescriptionImages
    ) {
        Long userId = (Long) request.getAttribute("userId");
        ProductResDto productResDto = productService.registerProduct(
                userId,
                ProductReqDto.from(productReqDto),
                productReqDto.items(),
                thumbnail, productImages, productDescriptionImages);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productResDto);
    }


    @Auth
    @PutMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "상품 정보 및 상품아이템 수정", description = "상품 정보와 옵션별 상품 아이템 데이터 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ProductResDto.class))),
            @ApiResponse(responseCode = "400", description = "실패",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    public ResponseEntity<ProductResDto> updateProduct(
            HttpServletRequest request,
            @PathVariable Long productId,
            @RequestPart("product") ProductReqDto productRegisterReqDto) {
        Long userId = (Long) request.getAttribute("userId");
        ProductResDto productResDto = productService.updateProduct(
                userId,
                productId,
                ProductReqDto.from(productRegisterReqDto),
                productRegisterReqDto.items());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productResDto);
    }


    @Auth
    @DeleteMapping("/{productId}")
    @Operation(summary = "상품 정보 및 상품아이템 삭제", description = "상품 정보와 옵션별 상품 아이템 데이터 전체 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "조회 데이터 없음"),
            @ApiResponse(responseCode = "400", description = "실패",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    public ResponseEntity<?> deleteProduct(
            HttpServletRequest request,
            @PathVariable Long productId) {
        Long userId = (Long) request.getAttribute("userId");
        productService.deleteProduct(userId, productId);
        return ResponseEntity.noContent().build();
    }
}
