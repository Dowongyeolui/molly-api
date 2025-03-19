package org.example.mollyapi.product.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.mollyapi.product.dto.request.ProductBulkItemReqDto;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ProductItemMapper {

    void insertProductItems(@Param("productItems") List<ProductBulkItemReqDto> productItems,
                            @Param("now") LocalDateTime now);

    List<Long> getProductsByIdRange(@Param("startId") Long startId,
                                    @Param("endId") Long endId);

}