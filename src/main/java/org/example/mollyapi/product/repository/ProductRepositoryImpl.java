package org.example.mollyapi.product.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.example.mollyapi.product.dto.ProductAndThumbnailDto;
import org.example.mollyapi.product.dto.ProductFilterCondition;
import org.example.mollyapi.product.dto.QProductAndThumbnailDto;
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
                        product.createdAt
                ))
                .distinct()
                .from(productItem)
                .join(productItem.product, product)
                .leftJoin(productImage).on(productImage.product.eq(product).and(productImage.isRepresentative.eq(true)))
                .where(
                        colorCodeEq(condition.getColorCode()),
                        sizeEq(condition.getSize()),
                        categoryIdEq(condition.getCategoryId()),
                        priceGoe(condition.getPriceGoe()),
                        priceLt(condition.getPriceLt()),
                        sellerIdEq(condition.getSellerId())
                )
                .orderBy(product.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }
        return new SliceImpl(content, pageable, hasNext);
    }
    private BooleanExpression colorCodeEq(String colorCode) {
        return hasText(colorCode)? productItem.colorCode.eq(colorCode) : null;
    }

    private BooleanExpression sizeEq(String size) {
        return hasText(size)? productItem.size.eq(size) : null;
    }

    private BooleanExpression categoryIdEq(List<Long> categoryId) {
        return categoryId != null && !categoryId.isEmpty()? product.category.id.in(categoryId) : null;
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

    private BooleanExpression getThumbnail() {
        return productImage.isRepresentative.eq(true);
    }
}
