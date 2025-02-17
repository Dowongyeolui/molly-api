package org.example.mollyapi.cart.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mollyapi.common.entity.Base;
import org.example.mollyapi.product.entity.ProductItem;
import org.example.mollyapi.user.entity.User;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "cart")
public class Cart extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;

    private Long quantity; //수량

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_CART_USER"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(name = "FK_CART_PRODUCTITEM"))
    private ProductItem productItem;

    public void updateQuantity(Long totalQuantity) {
        this.quantity = totalQuantity;
    }

    public boolean updateCart(ProductItem item, Long quantity) {
        boolean flag = false;

        if(!this.productItem.equals(item)) {
            this.productItem = item;
            flag = true;
        }
        if(!this.quantity.equals(quantity)) {
            this.quantity = quantity;
            flag = true;
        }

        return flag;
    }
}