package org.example.mollyapi.review.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mollyapi.common.entity.Base;
import org.example.mollyapi.product.dto.UploadFile;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "review_image")
public class ReviewImage extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_image_id")
    private Long id; //리뷰 이미지 PK

    @Column(nullable = false)
    private String url; //리뷰 이미지 url

    @Column(nullable = false)
    private String filename;

    @Column(name = "image_index", nullable = false)
    private Long imageIndex;

    @Column(name = "is_video", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isVideo; //비디오 여부. 0: False, 1: True

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @Builder
    public ReviewImage(
            UploadFile uploadFile,
            Long imageIndex,
            boolean isVideo,
            Review review
    ) {
        this.url = uploadFile.getUploadFileName();
        this.filename = uploadFile.getStoredFileName();
        this.imageIndex = imageIndex;
        this.isVideo = isVideo;
        this.review = review;
    }

    public static ReviewImage createReviewImage(Review review, UploadFile uploadFile, Long idx) {
        return new ReviewImage(uploadFile, idx, false, review);
    }
}