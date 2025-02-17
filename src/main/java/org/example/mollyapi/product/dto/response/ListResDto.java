package org.example.mollyapi.product.dto.response;

import org.springframework.data.domain.PageRequest;

import java.util.List;

public record ListResDto(
        PageResDto pageable,
        List<?> data
) {
}
