package org.example.mollyapi.product.dto.response;

import java.util.List;

public record ColorDetailDto(
    String color,
    String colorCode,
    List<SizeDetailDto> sizeDetails
    ) {}
