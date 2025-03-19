package org.example.mollyapi.product.service;

import org.example.mollyapi.product.dto.BrandSummaryDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface BrandService {
    Slice<BrandSummaryDto> getPopularBrand(Pageable pageable);
}
