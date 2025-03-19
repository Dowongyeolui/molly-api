package org.example.mollyapi.search.repository.impl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.mollyapi.search.dto.ItemDto;
import org.example.mollyapi.search.dto.SearchCommonResDto;
import org.example.mollyapi.search.dto.SearchItemResDto;
import org.example.mollyapi.search.dto.SearchOptionReqDto;
import org.example.mollyapi.search.repository.SearchCustomRepository;
import org.example.mollyapi.search.type.SortBy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.example.mollyapi.product.entity.QProduct.product;
import static org.example.mollyapi.product.entity.QProductImage.productImage;
import static org.example.mollyapi.product.entity.QProductItem.productItem;
import static org.example.mollyapi.search.entity.QSearch.search;
import static org.springframework.util.StringUtils.hasText;


@RequiredArgsConstructor
public class SearchCustomRepositoryImpl implements SearchCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public SearchItemResDto search(SearchOptionReqDto searchOptionReqDto, int pageSize) {

        String keyword = searchOptionReqDto.keyword();
        LocalDateTime lastCreatedAt = searchOptionReqDto.lastCreatedAt();
        Long cursorId = searchOptionReqDto.cursorId();
        String likeKeyword = "%" + keyword + "%";


        List<ItemDto> itemDtos = jpaQueryFactory
                .select(
                        Projections.constructor(ItemDto.class,
                                product.id,
                                productImage.url,
                                product.brandName,
                                product.productName,
                                product.price,
                                product.createdAt
                        )
                ).from(product)
                .innerJoin(productImage).on(
                        productImage.product.id.eq(product.id)
                                .and(productImage.isRepresentative.eq(true)))
                .innerJoin(productItem).on(product.id.eq(productItem.product.id))
                .where(
                        colorCodeEq(searchOptionReqDto.colorCode()),
                        sizeEq(searchOptionReqDto.size()),
                        categoryIdEq(searchOptionReqDto.categoryId()),
                        brandNameEq(searchOptionReqDto.brandName()),
                        searchKeyword(likeKeyword),
                        priceGoe(searchOptionReqDto.minPrice()),
                        priceLt(searchOptionReqDto.maxPrice()),
                        sortedPaging(lastCreatedAt, cursorId)

                )
                .orderBy(sortedByOption(searchOptionReqDto.sortOption()).toArray(new OrderSpecifier[0]))
                .groupBy(product.id)
                .limit(pageSize + 1)
                .fetch();


        boolean isLastPage = itemDtos.size() <= pageSize;
        if (!isLastPage) {
            itemDtos.remove(itemDtos.size() - 1);
        }


        Long id = null;
        LocalDateTime localDateTime = null;

        if (!itemDtos.isEmpty()) {
            ItemDto lastItemDto = itemDtos.get(itemDtos.size() - 1);
            id = lastItemDto.id();
            localDateTime = lastItemDto.lastCratedAt();
        }

        return new SearchItemResDto(
                itemDtos,
                id,
                localDateTime,
                isLastPage
        );
    }

    @Override
    public SearchCommonResDto searchAutoWord(String keyword) {

        String likeKeyword = "%" + keyword + "%";
        List<String> fetch = jpaQueryFactory.select(
                        search.keyword
                ).from(search)
                .where(search.keyword.like(likeKeyword))
                .limit(10)
                .fetch();
        return new SearchCommonResDto(fetch);
    }

    @Override
    public SearchCommonResDto searchBrand(String keyword) {
        String likeKeyword = "%" + keyword + "%";
        List<String> fetch = jpaQueryFactory.selectDistinct(
                        product.brandName
                ).from(product)
                .where(product.brandName.like(likeKeyword))
                .limit(10)
                .fetch();
        return new SearchCommonResDto(fetch);
    }

    private BooleanExpression colorCodeEq(List<String> colorCode) {
        return colorCode != null && !colorCode.isEmpty() ? productItem.colorCode.in(colorCode) : null;
    }

    private BooleanExpression sizeEq(List<String> size) {
        return size != null && !size.isEmpty() ? productItem.size.in(size) : null;
    }

    private BooleanExpression categoryIdEq(List<Long> categoryId) {
        return categoryId != null && !categoryId.isEmpty() ? product.category.id.in(categoryId) : null;
    }

    private BooleanExpression brandNameEq(List<String> brandName) {
        return brandName != null && !brandName.isEmpty() ? product.brandName.in(brandName) : null;
    }

    private BooleanExpression priceGoe(Long priceGoe) {
        return priceGoe != null ? product.price.goe(priceGoe) : null;
    }

    private BooleanExpression priceLt(Long priceLt) {
        return priceLt != null ? product.price.lt(priceLt) : null;
    }

    private BooleanExpression searchKeyword(String likeKeyword) {
        return hasText(likeKeyword) ?
                product.productName.like(likeKeyword)
                        .or(product.brandName.like(likeKeyword))
                        .or(product.description.like(likeKeyword)) : null;
    }

    private BooleanExpression sortedPaging(LocalDateTime lastCreatedAt, Long cursorId) {
        return lastCreatedAt != null && cursorId != null ?
                product.createdAt.lt(lastCreatedAt)
                        .or(product.createdAt
                                .eq(lastCreatedAt)
                                .and(product.id.lt(cursorId))
                        ) : product.id.isNotNull();
    }

    private List<OrderSpecifier<?>> sortedByOption(List<SortBy> sortOptions) {

        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        if (sortOptions != null && !sortOptions.isEmpty()) {
            for (SortBy sortBy : sortOptions) {
                switch (sortBy) {
                    case PRICE_DESC:
                        orderSpecifiers.add(product.price.desc());
                        break;
                    case PRICE_ASC:
                        orderSpecifiers.add(product.price.asc());
                        break;
                    case NEW_DESC:
                        orderSpecifiers.add(product.createdAt.desc());
                        break;
                    case NEW_ASC:
                        orderSpecifiers.add(product.createdAt.asc());
                        break;
                    case VIEW_DESC:
                        orderSpecifiers.add(product.viewCount.desc());
                        break;
                    case VIEW_ASC:
                        orderSpecifiers.add(product.viewCount.asc());
                        break;
                    case SELL_DESC:
                        orderSpecifiers.add(product.purchaseCount.desc());
                        break;
                    case SELL_ASC:
                        orderSpecifiers.add(product.purchaseCount.asc());
                        break;
                }
            }
        }

        orderSpecifiers.add(product.createdAt.desc());
        orderSpecifiers.add(product.id.desc());

        return orderSpecifiers;
    }

}


