package org.example.mollyapi.user.auth.entity;

import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.type.Role;
import org.example.mollyapi.user.type.Sex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
@ActiveProfiles("test")
class AuthTest {

    @Test
    @DisplayName("사용자의 마지막 로그인 시간을 업데이트한다")
    void updatedLastLoginAt() {
        //given
        Auth auth = createAuth();

        //when
        LocalDateTime loginAt = LocalDateTime.now();
        auth.updatedLastLoginAt(loginAt);

        //then
        assertEquals(loginAt, auth.getLastLoginAt());
    }

    private Auth createAuth() {
        return Auth.builder()
                .email("test@example.com")
                .role(List.of(Role.BUY))
                .password(null)
                .user(null)
                .build();
    }

}