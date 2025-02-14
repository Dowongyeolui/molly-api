package org.example.mollyapi.product.repository;

import org.example.mollyapi.product.entity.ProductItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductItemRepository extends JpaRepository<ProductItem, Long> {
    Optional<List<ProductItem>> findAllByProductId(Long productId);
}