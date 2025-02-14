package org.example.mollyapi.product.repository;

import org.example.mollyapi.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
}
