package org.example.mollyapi.user.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;



@Getter
@Embeddable
@NoArgsConstructor( access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Password {

    @Column(table = "password")
    private String password;

    @Column(columnDefinition = "BINARY(32)", table = "password")
    private byte[] salt;

    public static Password of(String password, byte[] salt) {
        return new Password(password, salt);
    }
}
