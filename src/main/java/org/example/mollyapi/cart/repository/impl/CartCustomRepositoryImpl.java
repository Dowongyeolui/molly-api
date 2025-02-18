package org.example.mollyapi.cart.repository.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.mollyapi.cart.dto.Response.CartInfoDto;
import org.example.mollyapi.cart.entity.QCart;
import org.example.mollyapi.cart.repository.CartCustomRepository;
import org.example.mollyapi.product.entity.QProduct;
import org.example.mollyapi.product.entity.QProductImage;
import org.example.mollyapi.product.entity.QProductItem;

import java.util.List;

@RequiredArgsConstructor
public class CartCustomRepositoryImpl implements CartCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<CartInfoDto> getCartInfo(Long userId) {
        QCart cart = QCart.cart;
        QProduct product = QProduct.product;
        QProductItem item = QProductItem.productItem;
        QProductImage image = QProductImage.productImage;

        return jpaQueryFactory.select(
                        Projections.constructor(CartInfoDto.class,
                                cart.cartId,
                                item.id,
                                item.color,
                                item.size,
                                product.id,
                                product.productName,
                                product.brandName,
                                product.price,
                                image.url,
                                cart.quantity
                        )).from(cart)
                .innerJoin(item).on(cart.productItem.eq(item))
                .innerJoin(product).on(item.product.eq(product))
                .innerJoin(image).on(product.id.eq(image.product.id)
                    .and(image.isRepresentative.eq(Boolean.TRUE)))
                .where(cart.user.userId.eq(userId))
                .fetch();
    }
}
