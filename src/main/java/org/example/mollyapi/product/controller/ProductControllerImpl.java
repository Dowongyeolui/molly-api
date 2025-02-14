package org.example.mollyapi.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.example.mollyapi.product.dto.response.ListResDto;
import org.example.mollyapi.product.service.ProductService;
import org.example.mollyapi.product.dto.request.ProductRegisterReqDto;
import org.example.mollyapi.product.dto.response.ProductResDto;
import org.example.mollyapi.user.auth.annotation.Auth;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Tag(name = "Product Controller", description = "상품 관련 엔드포인트")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductControllerImpl {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "상품 정보 목록",
            description = "상품 정보와 옵션별 상품 아이템 데이터 조회,  " +
                    "파라미터 예시: ?categories=여성,아우터")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 목록 반환",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResDto.class)))),
            @ApiResponse(responseCode = "204", description = "조회 데이터 없음", content = @Content(schema = @Schema(type = "string", example = ""))),
            @ApiResponse(responseCode = "400", description = "실패",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    public ResponseEntity<?> getAllProducts(
            @RequestParam(required = false) String categories
    ) {
        List<ProductResDto> products;
        if (categories == null) {
            products = productService.getAllProducts();
        } else {
            List<String> categoriesList = Arrays.stream(categories.split(",")).toList();
            products = productService.getProductsByCategory(categoriesList);
        }

        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ListResDto(products));
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
            @Valid @RequestPart("product") ProductRegisterReqDto productRegisterReqDto,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "productImages", required = false) List<MultipartFile> productImages,
            @RequestPart(value = "productDescriptionImages", required = false) List<MultipartFile> productDescriptionImages
    )  {
        Long userId = (Long) request.getAttribute("userId");
        ProductResDto productResDto = productService.registerProduct(userId, productRegisterReqDto, thumbnail, productImages, productDescriptionImages);
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
            @RequestPart("product") ProductRegisterReqDto productRegisterReqDto) {
        Long userId = (Long) request.getAttribute("userId");
        ProductResDto productResDto = productService.updateProduct(userId, productId, productRegisterReqDto);
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
