package org.example.mollyapi.product.dto.response;
import lombok.Getter;
import org.example.mollyapi.product.dto.ProductItemDto;
import java.util.List;

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
        List<ProductItemDto> items,
        List<ColorDetailDto> colorDetails
) {}
