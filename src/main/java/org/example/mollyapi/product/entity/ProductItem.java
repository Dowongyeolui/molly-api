package org.example.mollyapi.product.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.common.entity.Base;
import org.example.mollyapi.order.entity.OrderDetail;

import java.util.List;

@Slf4j
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductItem extends Base {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "item_id")
        Long id;

        String color;
        String colorCode;
        String size;
        Long quantity;

        @Setter
        @ManyToOne
        @JoinColumn(name = "product_id")
        Product product;

        @OneToMany(mappedBy = "productItem")
        List<OrderDetail> orderDetails;

        @Builder
        ProductItem(
                Long id,
                String color,
                String colorCode,
                String size,
                Long quantity,
                Product product) {
                this.id = id;
                this.color = color;
                this.colorCode = colorCode;
                this.size = size;
                this.quantity = quantity;
                this.product = product;
        }

        public void updateQuantity(Long quantity) {
                this.quantity = quantity;
        }

        public void decreaseStock(int quantityToDecrease) {
                if (this.quantity < quantityToDecrease) {
                        throw new IllegalArgumentException("재고 부족: 현재 수량=" + this.quantity + ", 요청 수량=" + quantityToDecrease);
                }
                this.quantity -= quantityToDecrease;
        }

        public void restoreStock(Long quantityToRestore) {
                this.quantity += quantityToRestore;
        }
}
