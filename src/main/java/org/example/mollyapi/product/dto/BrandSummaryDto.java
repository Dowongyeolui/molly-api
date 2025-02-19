package org.example.mollyapi.product.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class BrandSummaryDto {
    private String brandThumbnailUrl;
    private String brandName;
    private Long totalProductCount;
    private Long viewCount;

    @QueryProjection
    public BrandSummaryDto(
            String brandThumbnailUrl,
            String brandName,
            Long totalProductCount,
            Long viewCount) {
        this.brandThumbnailUrl = brandThumbnailUrl;
        this.brandName = brandName;
        this.totalProductCount = totalProductCount;
        this.viewCount = viewCount;
    }
}
