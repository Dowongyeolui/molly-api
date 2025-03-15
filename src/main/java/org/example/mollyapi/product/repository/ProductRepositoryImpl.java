package org.example.mollyapi.product.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.example.mollyapi.product.dto.*;
import org.example.mollyapi.product.enums.OrderBy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

import static org.example.mollyapi.product.entity.QProduct.product;
import static org.example.mollyapi.product.entity.QProductImage.productImage;
import static org.example.mollyapi.product.entity.QProductItem.productItem;
import static org.springframework.util.StringUtils.hasText;

public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @PersistenceContext
    private EntityManager entityManager;

    public ProductRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Slice<BrandSummaryDto> getTotalViewGroupByBrandName(Pageable pageable) {
        if (pageable == null) { pageable  = Pageable.unpaged(); }

        JPAQuery<BrandSummaryDto> query = queryFactory.select(
                        new QBrandSummaryDto(
                                productImage.url.max().as("brandThumbnail"),
                                product.brandName,
                                product.count(),
                                product.viewCount.sum().as("viewCount")))
                .from(productImage)
                .join(productImage.product, product)
                .on(productImage.isRepresentative.isTrue())
                .groupBy(product.brandName)
                .orderBy(product.viewCount.sum().desc());

        if (pageable.isPaged()) {
            query.offset(pageable.getOffset())
                    .limit(pageable.getPageSize() + 1);
        }
        List<BrandSummaryDto> content = query.fetch();

        boolean hasNext = false;
        if (pageable.isPaged() && content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }
        return new SliceImpl<>(content, pageable, hasNext);
    }

//    @Override
//    public Slice<ProductAndThumbnailDto> findByCondition(ProductFilterCondition condition, Pageable pageable) {
//        if (pageable == null) pageable = Pageable.unpaged();
//
//        JPAQuery<ProductAndThumbnailDto> query = queryFactory
//                .select(new QProductAndThumbnailDto(
//                        product.id,
//                        product.category.id.as("categoryId"),
//                        product.brandName,
//                        product.productName,
//                        product.price,
//                        product.createdAt,
//                        product.viewCount,
//                        product.purchaseCount,
//                        productImage.url,
//                        productImage.filename
//                ))
//                .from(product)
//                .join(productImage).on(productImage.product.eq(product).and(productImage.isRepresentative.eq(true)));
//
//        if (    condition != null && (
//                (condition.colorCode() != null && !condition.colorCode().isEmpty()) ||
//                (condition.size() != null && !condition.size().isEmpty()) ||
//                (condition.excludeSoldOut() != null && condition.excludeSoldOut() == Boolean.TRUE))){
//             query.where(product.id.in(
//                    JPAExpressions
//                            .select(productItem.product.id)
//                            .from(productItem)
//                            .where(
//                                    colorCodeEq(condition.colorCode()),
//                                    sizeEq(condition.size()),
//                                    excludeSoldOut(condition.excludeSoldOut())
//                            )
//                            .groupBy(productItem.product.id)));
//        }
//        if (condition != null) {
//            query.where(
//                            categoryIdEq(condition.categoryId()),
//                            brandNameEq(condition.brandName()),
//                            priceGoe(condition.priceGoe()),
//                            priceLt(condition.priceLt()),
//                            sellerIdEq(condition.sellerId()))
//                    .orderBy(orderByCondition(condition.orderBy()));
//        }
//
//        if (pageable.isPaged()) {
//            query.offset(pageable.getOffset());
//            query.limit(pageable.getPageSize() + 1);
//        }
//
//        List<ProductAndThumbnailDto> content = query.fetch();
//
//        boolean hasNext = false;
//        if (pageable.isPaged() && content.size() > pageable.getPageSize()) {
//            content.remove(pageable.getPageSize());
//            hasNext = true;
//        }
//        return new SliceImpl<>(content, pageable, hasNext);
//    }
    private BooleanExpression colorCodeEq(List<String> colorCode) {
        return colorCode != null && !colorCode.isEmpty()? productItem.colorCode.in(colorCode): null;
    }

    private BooleanExpression sizeEq(List<String> size) {
        return size != null && !size.isEmpty()? productItem.size.in(size) : null;
    }

    private BooleanExpression categoryIdEq(List<Long> categoryId) {
        return categoryId != null && !categoryId.isEmpty()? product.category.id.in(categoryId) : null;
    }

    private BooleanExpression brandNameEq(String brandName) {
        return hasText(brandName)? product.brandName.eq(brandName) : null;
    }

    private BooleanExpression priceGoe(Long priceGoe) {
        return priceGoe != null? product.price.goe(priceGoe) : null;
    }

    private BooleanExpression priceLt(Long priceLt) {
        return priceLt != null? product.price.lt(priceLt) : null;
    }

    private BooleanExpression sellerIdEq(Long sellerId) {
        return sellerId != null? product.user.userId.eq(sellerId) : null;
    }

    private BooleanExpression excludeSoldOut(Boolean excludeSoldOut) {
        return excludeSoldOut != null && excludeSoldOut.equals(Boolean.TRUE) ? productItem.quantity.gt(0): null;
    }

    private OrderSpecifier<?> orderByCondition(OrderBy orderBy) {
        if (orderBy != null) {
            return switch (orderBy) {
                case CREATED_AT -> product.createdAt.desc();
                case VIEW_COUNT -> product.viewCount.desc();
                case PURCHASE_COUNT -> product.purchaseCount.desc();
                case PRICE_DESC ->  product.price.desc();
                case PRICE_ASC -> product.price.asc();
            };
        }
        return product.createdAt.desc();
    }

    @Override
    public Slice<ProductAndThumbnailDto> findByCondition(ProductFilterCondition condition, Pageable pageable) {
        if (pageable == null) pageable = Pageable.unpaged();

        StringBuilder jpql = new StringBuilder("SELECT new org.example.mollyapi.product.dto.ProductAndThumbnailDto(" +
                "p.id, p.category.id, p.brandName, p.productName, p.price, p.createdAt, " +
                "p.viewCount, p.purchaseCount, pi.url, pi.filename) " +
                "FROM Product p " +
                "JOIN ProductImage pi ON pi.product = p AND pi.isRepresentative = true ");

        // 조건이 있을 경우 서브쿼리 추가
        if (condition != null) {

            // colorCode 조건이 있을 경우
            if (condition.colorCode() != null && !condition.colorCode().isEmpty()) {
                jpql.append("WHERE p.id IN (SELECT pi.product.id FROM ProductItem pi " +
                        "WHERE pi.colorCode IN :colorCode ");
            }

            // size 조건이 있을 경우
            if (condition.size() != null && !condition.size().isEmpty()) {
                if (jpql.toString().contains("WHERE")) {
                    jpql.append("AND pi.size IN :size ");
                } else {
                    jpql.append("WHERE p.id IN (SELECT pi.product.id FROM ProductItem pi " +
                            "WHERE pi.size IN :size ");
                }
            }

            // excludeSoldOut 조건이 있을 경우
            if (condition.excludeSoldOut() != null && condition.excludeSoldOut() == Boolean.TRUE) {
                if (jpql.toString().contains("WHERE")) {
                    jpql.append("AND pi.quantity > 0 ");
                } else {
                    jpql.append("WHERE p.id IN (SELECT pi.product.id FROM ProductItem pi " +
                            "WHERE pi.quantity > 0 ");
                }
            }

            // 서브쿼리 끝내기
            if (jpql.toString().contains("WHERE")) {
                jpql.append("GROUP BY pi.product.id) ");
            }
        }

        // 추가적인 조건들
        if (condition != null) {
            if (condition.categoryId() != null) {
                if (jpql.toString().contains("WHERE")) {
                    jpql.append("AND p.category.id IN :categoryId ");
                } else {
                    jpql.append("WHERE p.category.id IN :categoryId ");
                }
            }
            if (condition.brandName() != null && !condition.brandName().isEmpty()) {
                if (jpql.toString().contains("WHERE")) {
                    jpql.append("AND p.brandName = :brandName ");
                } else {
                    jpql.append("WHERE p.brandName = :brandName ");
                }
            }
            if (condition.priceGoe() != null) {
                if (jpql.toString().contains("WHERE")) {
                    jpql.append("AND p.price >= :priceGoe ");
                } else {
                    jpql.append("WHERE p.price >= :priceGoe ");
                }
            }
            if (condition.priceLt() != null) {
                if (jpql.toString().contains("WHERE")) {
                    jpql.append("AND p.price < :priceLt ");
                } else {
                    jpql.append("WHERE p.price < :priceLt ");
                }
            }
            if (condition.sellerId() != null) {
                if (jpql.toString().contains("WHERE")) {
                    jpql.append("AND p.seller.id = :sellerId ");
                } else {
                    jpql.append("WHERE p.seller.id = :sellerId ");
                }
            }
        }
        // 기본적으로 createdAt 내림차순 정렬
        String orderByClause = "p.createdAt DESC"; // 기본값으로 createdAt 내림차순 설정

// condition.orderBy()에 따라 정렬 변경
        if (condition != null && condition.orderBy() != null) {
            switch (condition.orderBy()) {
                case CREATED_AT: // createdAt 내림차순
                    orderByClause = "p.createdAt DESC";
                    break;
                case VIEW_COUNT: // viewCount 내림차순
                    orderByClause = "p.viewCount DESC";
                    break;
                case PURCHASE_COUNT: // purchaseCount 내림차순
                    orderByClause = "p.purchaseCount DESC";
                    break;
                case PRICE_DESC: // price 내림차순
                    orderByClause = "p.price DESC";
                    break;
                case PRICE_ASC: // price 오름차순
                    orderByClause = "p.price ASC";
                    break;
                default:
                    // 기본적으로 createdAt 내림차순을 설정했으므로, 추가적인 조건에 대해 처리할 필요 없음
                    break;
            }
        }

// 최종적으로 jpql에 정렬 기준을 추가
        jpql.append("ORDER BY ").append(orderByClause);

        // 페이징 처리
        TypedQuery<ProductAndThumbnailDto> query = entityManager.createQuery(jpql.toString(), ProductAndThumbnailDto.class);

// 파라미터 설정
        if (condition != null) {
            // colorCode가 비어있지 않으면 설정
            if (condition.colorCode() != null && !condition.colorCode().isEmpty()) {
                query.setParameter("colorCode", condition.colorCode());
            }

            // size가 비어있지 않으면 설정
            if (condition.size() != null && !condition.size().isEmpty()) {
                query.setParameter("size", condition.size());
            }

            // categoryId가 null이 아니면 설정
            if (condition.categoryId() != null) {
                query.setParameter("categoryId", condition.categoryId());
            }

            // brandName이 null이 아니면 설정
            if (condition.brandName() != null && !condition.brandName().isEmpty()) {
                query.setParameter("brandName", condition.brandName());
            }

            // priceGoe가 null이 아니면 설정
            if (condition.priceGoe() != null) {
                query.setParameter("priceGoe", condition.priceGoe());
            }

            // priceLt가 null이 아니면 설정
            if (condition.priceLt() != null) {
                query.setParameter("priceLt", condition.priceLt());
            }

            // sellerId가 null이 아니면 설정
            if (condition.sellerId() != null) {
                query.setParameter("sellerId", condition.sellerId());
            }
        }
        if (pageable.isPaged()) {
            // 페이징 처리
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize() + 1);  // 페이지당 하나 더 가져오기
        }
        List<ProductAndThumbnailDto> content = query.getResultList();

        boolean hasNext = false;
        if (pageable.isPaged() && content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }
}
