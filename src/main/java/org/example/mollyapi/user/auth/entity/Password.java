package org.example.mollyapi.user.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Embeddable
@NoArgsConstructor( access = AccessLevel.PROTECTED)
public class Password {

    @Column(table = "password")
    private String password;

    @Column(columnDefinition = "BINARY(32)", table = "password")
    private byte[] salt;

    @Builder
    public Password(String password, byte[] salt) {
        this.password = password;
        this.salt = salt;
    }

    public static Password createPassword(String password, byte[] salt) {
        return Password.builder()
                .password(password)
                .salt(salt)
                .build();
    }
}
