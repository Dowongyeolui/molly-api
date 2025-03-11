package org.example.mollyapi.product.service;

import jakarta.transaction.Transactional;
import org.example.mollyapi.product.dto.BrandSummaryDto;
import org.example.mollyapi.product.dto.ProductFilterCondition;
import org.example.mollyapi.product.dto.response.ProductResDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductReadServiceImpl implements ProductReadService {

    @Override
    @Transactional
    public Slice<ProductResDto> getAllProducts(ProductFilterCondition condition, Pageable pageable) {
        return null;
//        Slice<ProductAndThumbnailDto> page = productRepository.findByCondition(condition, pageable);
//
//        return page.map(this::convertToProductResDto);
    }

    @Override
    public Slice<BrandSummaryDto> getPopularBrand(Pageable pageable) {
        return null;
//        return productRepository.getTotalViewGroupByBrandName(pageable);
    }

    @Override
    @Transactional
    public Optional<ProductResDto> getProductById(Long id) {
        return null;
//        Optional<Product> product = productRepository.findById(id);
//
//        // 조회수 증가
//        product.ifPresent(Product::increaseViewCount);
//
//        return product.map(this::convertToProductResDto);
    }



}
