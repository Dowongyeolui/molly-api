package org.example.mollyapi.product.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import org.example.mollyapi.product.entity.ProductImage;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductAndThumbnailDto {
    Long id;
    Long categoryId;
    String brandName;
    String productName;
    Long price;
    String description;
//    ProductImage thumbnail;
    String url;
    String filename;
    LocalDateTime createdAt;


    @QueryProjection
    public ProductAndThumbnailDto(
            Long id,
            Long categoryId,
            String brandName,
            String productName,
            Long price,
            String description,
//            ProductImage thumbnail
            String url,
            String filename,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.categoryId = categoryId;
        this.brandName = brandName;
        this.productName = productName;
        this.price = price;
        this.description = description;
        this.url = url;
        this.filename = filename;
        this.createdAt = createdAt;
    }
}
