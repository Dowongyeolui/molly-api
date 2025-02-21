package org.example.mollyapi.review.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mollyapi.common.entity.Base;
import org.example.mollyapi.order.entity.OrderDetail;
import org.example.mollyapi.product.entity.Product;
import org.example.mollyapi.user.entity.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "review")
public class Review extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id; //리뷰 PK

    @Column(length = 2200, nullable = false)
    private String content; //리뷰 내용

    @Column(name = "is_deleted", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isDeleted; //삭제 여부. 0: False, 1: True

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_REVIEW_USER"))
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_detail_id", nullable = false, foreignKey = @ForeignKey(name = "FK_REVIEW_ORDERDETAIL"))
    private OrderDetail orderDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "FK_REVIEW_PRODUCT"))
    private Product product;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewImage> reviewImages = new ArrayList<>(); //리뷰 이미지 리스트

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewLike> reviewLike = new ArrayList<>(); //리뷰 좋아요 리스트

    public void addImage(ReviewImage reviewImage) {
        this.reviewImages.add(reviewImage);
    }

    public void updateImages(List<ReviewImage> reviewImage) {
        this.reviewImages = reviewImage;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public boolean updateIsDeleted(Boolean isDeleted) {
        boolean flag = false;

        if(!this.isDeleted.equals(isDeleted)) {
            this.isDeleted = isDeleted;
            flag = true;
        }

        return flag;
    }
}
