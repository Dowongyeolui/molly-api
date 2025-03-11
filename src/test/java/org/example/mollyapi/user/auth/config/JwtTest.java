package org.example.mollyapi.user.auth.config;

import org.example.mollyapi.user.type.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class JwtTest {

    @Autowired
    private Jwt jwt;

    @Test
    @DisplayName("주어진 정보를 통해 토큰을 생성할 수 있다.")
    void generateToken() {
        //given
        Long authId = 1L;
        Long userId = 1L;
        String givenEmail = "test@example.com";
        List<Role> roles = List.of(Role.BUY, Role.SELL);

        //when
        String token = jwt.generateToken(authId, userId, givenEmail, roles);

        //then
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("토큰에서 이메일을 추출한다")
    void extractMemberEmail() {
        //given
        Long authId = 1L;
        Long userId = 1L;
        String givenEmail = "test@example.com";
        List<Role> givenRoles = List.of(Role.BUY, Role.SELL);

        //when
        String token = jwt.generateToken(authId, userId, givenEmail, givenRoles);
        String extractedEmail = jwt.extractMemberEmail(token);

        //then
        assertThat(extractedEmail).isEqualTo(givenEmail);

    }

    @Test
    @DisplayName("토큰에서 authId를 추출한다")
    void extractAuthId() {
        //given
        Long givenAuthId = 1L;
        Long givenUserId = 1L;
        String givenEmail = "test@example.com";
        List<Role> givenRoles = List.of(Role.BUY, Role.SELL);

        //when
        String token = jwt.generateToken(givenAuthId, givenUserId, givenEmail, givenRoles);
        Long extractedAuthId = jwt.extractAuthId(token);

        //then
        assertThat(extractedAuthId).isEqualTo(givenAuthId);
    }

    @Test
    @DisplayName("토큰에서 userId 추출한다")
    void extractUserId() {
        //given
        Long givenAuthId = 1L;
        Long givenUserId = 1L;
        String givenEmail = "test@example.com";
        List<Role> givenRoles = List.of(Role.BUY, Role.SELL);

        //when
        String token = jwt.generateToken(givenAuthId, givenUserId, givenEmail, givenRoles);
        Long extractedUserId = jwt.extractUserId(token);

        //then
        assertThat(extractedUserId).isEqualTo(givenUserId);
    }

    @Test
    @DisplayName("토큰에서 유효시간을 추출한다")
    void extractExpiration() {
        //given
        Long givenAuthId = 1L;
        Long givenUserId = 1L;
        String givenEmail = "test@example.com";
        List<Role> givenRoles = List.of(Role.BUY, Role.SELL);

        //when
        String token = jwt.generateToken(givenAuthId, givenUserId, givenEmail, givenRoles);
        Date extractDate = jwt.extractExpiration(token);

        //then
        assertThat(extractDate.after(new Date())).isTrue();

    }

    @Test
    @DisplayName("토큰에서 사용자 권한을 추출한다")
    void extractRole() {

        //given
        Long givenAuthId = 1L;
        Long givenUserId = 1L;
        String givenEmail = "test@example.com";
        List<Role> givenRoles = List.of(Role.BUY, Role.SELL);

        //when
        String token = jwt.generateToken(givenAuthId, givenUserId, givenEmail, givenRoles);
        List<String> roles = jwt.extractRole(token);

        //then
        assertThat(roles).hasSize(2);
        assertThat(roles).containsExactly("BUY", "SELL");
    }
}