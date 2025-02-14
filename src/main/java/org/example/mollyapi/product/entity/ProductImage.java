package org.example.mollyapi.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mollyapi.common.entity.Base;
import org.example.mollyapi.product.dto.UploadFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    Long id;
    String url;
    String filename;
    Boolean isProductImage;
    Boolean isRepresentative;
    Boolean isDescriptionImage;
    Long imageIndex;

    @Setter
    @ManyToOne
    @JoinColumn(name = "product_id")
    Product product;

    @Builder
    ProductImage(UploadFile uploadFile,
                 Boolean isProductImage,
                 Boolean isRepresentative,
                 Boolean isDescriptionImage,
                 Long imageIndex,
                 Product product
    ) {
        this.url = uploadFile.getStoredFileName();
        this.filename = uploadFile.getUploadFileName();
        this.isProductImage = isProductImage;
        this.isRepresentative = isRepresentative;
        this.isDescriptionImage = isDescriptionImage;
        this.imageIndex = imageIndex;
        this.product = product;
    }

    public static ProductImage createThumbnail(Product product, UploadFile uploadFile) {
        return new ProductImage(uploadFile, false, true, false, 0L, product);
    }

    public static ProductImage createProductImage(Product product, UploadFile uploadFile, long idx) {
        return new ProductImage(uploadFile, true, false, false, idx, product);
    }

    public static ProductImage createDescriptionImage(Product product, UploadFile uploadFile, long idx) {
        return new ProductImage(uploadFile, false, false, true, idx, product);
    }
}
