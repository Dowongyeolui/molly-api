package org.example.mollyapi.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.mollyapi.product.enums.OrderBy;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductFilterCondition {
    List<String> colorCode;
    List<String> size;

    List<Long> categoryId;
    String brandName;
    Long priceGoe;
    Long priceLt;
    Long sellerId;

    OrderBy orderBy;

    Boolean excludeSoldOut;
}
