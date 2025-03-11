package org.example.mollyapi.product.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ProductAndThumbnailDto {
    Long id;
    Long categoryId;
    String brandName;
    String productName;
    Long price;
    String description;
    String colorCode;
    String size;
    Long quantity;
    String url;
    String filename;
    LocalDateTime createdAt;
    Long viewCount;
    Long purchaseCount;
    Boolean excludeSoldOut;


    @QueryProjection
    public ProductAndThumbnailDto(
            Long id,
            Long categoryId,
            String brandName,
            String productName,
            Long price,
            String description,
            String colorCode,
            String size,
            Long quantity,
            String url,
            String filename,
            LocalDateTime createdAt,
            Long viewCount,
            Long purchaseCount

    ) {
        this.id = id;
        this.categoryId = categoryId;
        this.brandName = brandName;
        this.productName = productName;
        this.price = price;
        this.description = description;
        this.colorCode = colorCode;
        this.size = size;
        this.quantity = quantity;
        this.url = url;
        this.filename = filename;
        this.createdAt = createdAt;
        this.viewCount = viewCount;
        this.purchaseCount = purchaseCount;
    }
}
