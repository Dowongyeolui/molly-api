package org.example.mollyapi.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.mollyapi.product.entity.Product;
import org.example.mollyapi.product.entity.ProductItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public record ProductResDto(
        Long id,
        List<String> categories,
        String brandName,
        String productName,
        Long price,
        String description,
        FileInfoDto thumbnail,
        List<FileInfoDto> productImages,
        List<FileInfoDto> productDescriptionImages,
        List<ProductItemResDto> items,
        List<ColorDetailDto> colorDetails
) {

    @Data
    @AllArgsConstructor
    static public class SizeDetail{
        Long id;
        String size;
        Long quantity;
    }

    @Data
    @AllArgsConstructor
    static public class ColorDetail {
        String color;
        String colorCode;
        List<SizeDetail> sizeDetails;
    }

    public static List<ColorDetail> groupItemByColor(List<ProductItem> items) {
        return items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getColor() + "_" + item.getColorCode(), // 그룹화 키 생성
                        LinkedHashMap::new, // 순서를 유지하는 맵 사용
                        Collectors.toList()
                ))
                .values()
                .stream()
                .map(groupedItems -> new ColorDetail(
                        groupedItems.get(0).getColor(),
                        groupedItems.get(0).getColorCode(),
                        groupedItems.stream()
                                .map(item -> new SizeDetail(item.getId(), item.getSize(), item.getQuantity()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }
}
