package org.example.mollyapi.review.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mollyapi.user.entity.User;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "review_like")
public class ReviewLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id; //좋아요 PK

    @Column(name = "is_like", columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isLike; //좋아요 여부. 0: False, 1: True

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_LIKE_USER"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false, foreignKey = @ForeignKey(name = "FK_LIKE_REVIEW"))
    private Review review;
}
