package org.example.mollyapi.product.repository;

import jakarta.persistence.LockModeType;
import org.example.mollyapi.product.entity.ProductItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductItemRepository extends JpaRepository<ProductItem, Long> {
    List<ProductItem> findAllByProductId(Long productId);

//    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ProductItem> findById(Long id);

    // 재고 조회 + 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductItem p WHERE p.id = :id")
    Optional<ProductItem> findByIdWithLock(@Param("id") Long id);

    @Lock(LockModeType.OPTIMISTIC)  // 낙관적 락 적용
    @Query("SELECT p FROM ProductItem p WHERE p.id = :id")
    Optional<ProductItem> findByIdWithOptimisticLock(@Param("id") Long id);

    default ProductItem findProductItemById(Long itemId) { // 일반 조회용
        return findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. itemId=" + itemId));
    }

    @Query("SELECT p FROM ProductItem p WHERE p.id = :productId")
    Optional<ProductItem> findWithOutLById(@Param("productId") Long productId);

    @Query("SELECT pi FROM ProductItem pi JOIN FETCH pi.product WHERE pi.id = :id")
    Optional<ProductItem> findByIdWithProduct(@Param("id") Long id);

}