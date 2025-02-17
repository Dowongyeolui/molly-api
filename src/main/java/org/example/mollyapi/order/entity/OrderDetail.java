package org.example.mollyapi.order.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mollyapi.product.entity.Product;
import org.example.mollyapi.product.entity.ProductItem;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_details")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private ProductItem productItem;

    @Column(nullable = true)
    private String size;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Long quantity;

    private String brandName;

    private String productName;

    @Builder
    public OrderDetail(Order order, ProductItem productItem, String size, Long price, Long quantity,String brandName, String productName) {
        this.order = order;
        this.productItem = productItem;
        this.size = size;
        this.price = price;
        this.quantity = quantity;
        this.brandName =brandName;
        this.productName = productName;
    }
}