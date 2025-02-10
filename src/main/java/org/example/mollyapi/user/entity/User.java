package org.example.mollyapi.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mollyapi.common.entity.Base;
import org.example.mollyapi.user.auth.entity.Auth;
import org.example.mollyapi.user.type.Sex;

import java.time.LocalDate;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "user")
public class User extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(length = 11, nullable = false)
    private String cellPhone;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Sex sex;

    @Column(nullable = false)
    private Boolean flag;

    private String profileImage;

    private LocalDate birth;

    private Integer point;

    private String name;

    @OneToOne
    @JoinColumn(name = "auth_id", nullable = false, foreignKey = @ForeignKey(name = "FK_USER_AUTH"))
    private Auth auth;

}
