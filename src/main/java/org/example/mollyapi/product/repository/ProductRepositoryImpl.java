package org.example.mollyapi.product.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
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

    public ProductRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Slice<BrandSummaryDto> getTotalViewGroupByBrandName(Pageable pageable) {
        List<BrandSummaryDto> content = queryFactory.select(
                new QBrandSummaryDto(
                        productImage.url.max().as("brandThumbnail"),
                        product.brandName,
                        product.count(),
                        product.viewCount.sum().as("viewCount")))
                .from(productImage)
                .join(productImage.product, product)
                .on(productImage.isRepresentative.isTrue())
                .groupBy(product.brandName)
                .orderBy(product.viewCount.sum().desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();
        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }
        return new SliceImpl<BrandSummaryDto>(content, pageable, hasNext);
    }

    @Override
    public Slice<ProductAndThumbnailDto> findByCondition(ProductFilterCondition condition, Pageable pageable) {
        List<ProductAndThumbnailDto> content = queryFactory
                .select(new QProductAndThumbnailDto(
                        product.id,
                        product.category.id.as("categoryId"),
                        product.brandName,
                        product.productName,
                        product.price,
                        product.description,
                        productImage.url,
                        productImage.filename,
                        product.createdAt,
                        product.viewCount,
                        product.purchaseCount
                ))
                .distinct()
                .from(productItem)
                .join(productItem.product, product)
                .leftJoin(productImage).on(productImage.product.eq(product).and(productImage.isRepresentative.eq(true)))
                .where(
                        colorCodeEq(condition.getColorCode()),
                        sizeEq(condition.getSize()),
                        categoryIdEq(condition.getCategoryId()),
                        brandNameEq(condition.getBrandName()),
                        priceGoe(condition.getPriceGoe()),
                        priceLt(condition.getPriceLt()),
                        sellerIdEq(condition.getSellerId()),
                        excludeSoldOut(condition.getExcludeSoldOut())
                )
                .orderBy(orderByCondition(condition.getOrderBy()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }
        return new SliceImpl<ProductAndThumbnailDto>(content, pageable, hasNext);
    }
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
                case CREATED_AT -> product.category.createdAt.desc();
                case VIEW_COUNT -> product.viewCount.desc();
                case PURCHASE_COUNT -> product.purchaseCount.desc();
                case PRICE_DESC ->  product.price.desc();
                case PRICE_ASC -> product.price.asc();
            };
        }
        return product.createdAt.desc();
    }
}
