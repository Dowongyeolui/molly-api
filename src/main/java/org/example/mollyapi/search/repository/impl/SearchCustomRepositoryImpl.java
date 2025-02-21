package org.example.mollyapi.search.repository.impl;

import com.querydsl.core.BooleanBuilder;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.mollyapi.search.dto.SearchCommonResDto;
import org.example.mollyapi.search.dto.ItemDto;
import org.example.mollyapi.search.dto.SearchItemResDto;
import org.example.mollyapi.search.repository.SearchCustomRepository;


import java.time.LocalDateTime;
import java.util.List;

import static org.example.mollyapi.product.entity.QProduct.product;
import static org.example.mollyapi.product.entity.QProductImage.*;
import static org.example.mollyapi.search.entity.QSearch.*;


@RequiredArgsConstructor
public class SearchCustomRepositoryImpl implements SearchCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public SearchItemResDto search(String keyword,
                                   Long cursorId,
                                   LocalDateTime lastCreatedAt,
                                   int pageSize) {

        BooleanBuilder condition = new BooleanBuilder();
        String likeKeyword = "%" + keyword + "%";

        if (keyword != null && !keyword.isEmpty()) {
            //값이 있을 때만, 검색
            condition.or(product.productName.like(likeKeyword))
                    .or(product.brandName.like(likeKeyword))
                    .or(product.description.like(likeKeyword));
        }

        // 값 중복을 피하기 위한 조건 값
        if (lastCreatedAt != null && cursorId != null) {
            condition.and(
                    product.createdAt.lt(lastCreatedAt)
                            .or(
                                    product.createdAt.eq(lastCreatedAt)
                                            .and(product.id.lt(cursorId))
                            )
            );
        } else {
            // 첫 페이지의 경우 정렬을 명확히 하기 위해 createdAt, id 정렬 유지
            condition.and(product.id.isNotNull());
        }

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
                .orderBy(product.createdAt.desc(), product.id.desc())
                .where(condition)
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

        System.out.println("Condition: " + condition);
        System.out.println("Query result size: " + itemDtos.size());
        System.out.println("Is last page: " + isLastPage);

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


}
