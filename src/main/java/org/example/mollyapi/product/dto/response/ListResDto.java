package org.example.mollyapi.product.dto.response;

import java.util.List;

public record ListResDto(
        PageResDto pageable,
        List<?> data
) {
}
