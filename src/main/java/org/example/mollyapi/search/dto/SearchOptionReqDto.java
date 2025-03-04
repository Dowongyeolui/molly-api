package org.example.mollyapi.search.dto;

import io.swagger.v3.oas.annotations.Parameter;
import org.example.mollyapi.search.type.SortBy;
import org.example.mollyapi.user.type.Sex;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;


public record SearchOptionReqDto (
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false, name = "cursor_id") Long cursorId,
        @RequestParam(required = false, name = "last_created_at") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime lastCreatedAt,
        @RequestParam(required = false) Integer pageSize,
        @Parameter(description = "요청 형식: sortOption=PRICE_DESC sortOption=SELL_ASC 옵션 : [PRICE,VIEW,SELL,NEW _ ASC, DESC]")
        @RequestParam(required = false, name = "sort_option") List<SortBy> sortOption,
        @Parameter(description = "MALE OR FEMALE")
        @RequestParam(required = false) Sex sex,
        @Parameter(description = "[남자:2, 여3] OR [아우터:4,상위:5,바지:6 / 아우터:8,상위:9,바지:10,원피스:11,패션소품:12]")
        @RequestParam(required = false, name = "category_id") List<Long> categoryId,
        @RequestParam(required = false, name = "color_code") List<String> colorCode,
        @RequestParam(required = false) List<String> size,
        @RequestParam(required = false, name = "min_price") Long minPrice,
        @RequestParam(required = false, name = "max_price") Long maxPrice,
        @RequestParam(required = false, name = "brand_name") List<String> brandName
        ){
}
