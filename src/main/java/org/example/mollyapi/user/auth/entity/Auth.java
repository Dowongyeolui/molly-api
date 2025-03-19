package org.example.mollyapi.user.auth.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.mollyapi.common.entity.Base;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.type.Role;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Table(name = "auth")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SecondaryTable(
        name = "password",
        pkJoinColumns = @PrimaryKeyJoinColumn( name = "auth_id") )
public class Auth extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long authId;

    private String email;

    private LocalDateTime lastLoginAt;

    @Embedded
    private Password password;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Role> role;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_AUTH_USER"))
    private User user;

    @Builder
    public Auth(String email, Password password, List<Role> role, User user) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.user = user;
    }

    public void updatedLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}
