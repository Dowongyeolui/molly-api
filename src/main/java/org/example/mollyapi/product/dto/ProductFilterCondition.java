package org.example.mollyapi.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductFilterCondition {
    String colorCode;
    String size;

    List<Long> categoryId;
//    String brandName;
    Long priceGoe;
    Long priceLt;
    Long sellerId;
}
