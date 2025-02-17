package org.example.mollyapi.product.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.mollyapi.common.entity.Base;
import org.example.mollyapi.product.dto.UploadFile;
import org.example.mollyapi.user.entity.User;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class Product extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    Long id;

    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;
    String brandName;
    String productName;
    Long price;
    String description;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    List<ProductItem> items = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @Builder
    public Product(
            Category category,
            String brandName,
            String productName,
            Long price,
            String description,
            List<ProductItem> items,
            List<ProductImage> images,
            User user
    ) {
        this.category = category;
        this.brandName = brandName;
        this.productName = productName;
        this.price = price;
        this.description = description;
        this.items = items;
        this.images = images;
        this.user = user;

        for (ProductImage image : images) {
            image.setProduct(this);
        }

        for (ProductItem item : items) {
            item.setProduct(this);
        }
    }

    public UploadFile getThumbnail() {
        ProductImage productImage = images.stream()
                .filter(img -> img.isRepresentative)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No product image found"));

        return new UploadFile(productImage.url, productImage.filename);
    }

    public List<UploadFile> getProductImages() {
        return images.stream()
                .filter(img -> img.isProductImage)
                .map(img -> new UploadFile(img.url, img.filename))
                .toList();
    }

    public List<UploadFile> getDescriptionImages() {
        return images.stream()
                .filter(img -> img.isDescriptionImage)
                .map(img -> new UploadFile(img.url, img.filename))
                .toList();
    }

    public Product update(
            Category category,
            String brandName,
            String productName,
            Long price,
            String description,
            List<ProductItem> items,
            List<ProductImage> images
    ) {
        this.category = category;
        this.brandName = brandName;
        this.productName = productName;
        this.price = price;
        this.description = description;
        
        // 아이템에서 변경된 재고 반영
        for (ProductItem updateItem : items) {
            for (ProductItem thisItem : this.items) {
                if (updateItem.getId().equals(thisItem.getId())) {
                    thisItem.updateQuantity(updateItem.quantity);
                }
            }
        }

        return this;
    }
}
