package org.example.mollyapi.product.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.mollyapi.product.dto.request.ProductBulkReqDto;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ProductMapper {

    void insertProducts(@Param("products") List<ProductBulkReqDto> products,
                        @Param("userId") Long userId,
                        @Param("now") LocalDateTime now);

}
