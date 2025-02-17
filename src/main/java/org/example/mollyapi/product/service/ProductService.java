package org.example.mollyapi.product.service;
import org.example.mollyapi.product.dto.request.ProductRegisterReqDto;
import org.example.mollyapi.product.dto.response.ColorDetailDto;
import org.example.mollyapi.product.dto.response.ProductResDto;
import org.example.mollyapi.product.entity.Product;
import org.example.mollyapi.product.entity.ProductItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    Slice<ProductResDto> getAllProducts(Pageable pageable);
    Slice<ProductResDto> getProductsByCategory(List<String> categories, Pageable pageable);

    Optional<ProductResDto> getProductById(Long id);
    ProductResDto registerProduct(
            Long userId,
            ProductRegisterReqDto productRegisterReqDto,
            MultipartFile thumbnailImage,
            List<MultipartFile> productImages,
            List<MultipartFile> descriptionImages
            );
    ProductResDto updateProduct(Long userId, Long id, ProductRegisterReqDto productRegisterReqDto);
    void deleteProduct(Long userId, Long id);

    List<ColorDetailDto> groupItemByColor(List<ProductItem> items);
    ProductResDto convertToProductResDto(Product product);
}