package org.example.mollyapi.product.dto.response;

public record PageResDto(
        Long size,
        Boolean hasNext,
        Boolean isFirst,
        Boolean isLast
) {
}
