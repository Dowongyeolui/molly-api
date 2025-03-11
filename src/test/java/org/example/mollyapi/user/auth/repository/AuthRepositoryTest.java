package org.example.mollyapi.user.auth.repository;

import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.user.auth.config.PasswordEncoder;
import org.example.mollyapi.user.auth.entity.Auth;
import org.example.mollyapi.user.auth.entity.Password;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.example.mollyapi.user.type.Role;
import org.example.mollyapi.user.type.Sex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class AuthRepositoryTest {

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("이메일로 사용자를 조회할 때 존재하면 Optional에 감싸진 Auth 객체를 반환한다.")
    void findByEmail() {
        //given
        Password password = createPassword();
        User user = createUser();
        User savedUser = userRepository.save(user);

        Auth auth = createAuth(password, savedUser);

        authRepository.save(auth);

        //when
        Optional<Auth> findUser = authRepository.findByEmail(auth.getEmail());

        //then
        assertThat(findUser)
                .isPresent()
                .get()
                .extracting(Auth::getEmail)
                .isEqualTo(auth.getEmail());
    }

    @Test
    @DisplayName("이메일로 사용자를 조회할 때 존재하지 않으면, 존재하지 않는 회원이다")
    void findByEmail_NotFound() {
        //given
        String testMail = "test@gmail.com";
        //when//then
        assertThat(authRepository.findByEmail(testMail)).isEmpty();

    }

    @Test
    @DisplayName("이메일로 사용자를 조회 할 때 존재하면 존재하는 회원이다")
    void existsByEmail() {
        //given
        User savedUser = userRepository.save(createUser());
        Password createdPassword = createPassword();
        Auth auth = createAuth(createdPassword, savedUser);

        authRepository.save(auth);

        //when //then
        assertTrue(authRepository.existsByEmail(auth.getEmail()));
    }

    @Test
    @DisplayName("이메일로 사용자를 조회 할 때 존재하면 존재하지 않는 회원이다")
    void existsByEmail_NotFound() {
        //given
        String testMail = "test@gmail.com";
        //when //then
        assertFalse(authRepository.existsByEmail(testMail));
    }


    private Auth createAuth(Password password, User user) {
        return Auth.builder()
                .email("test@example.com")
                .role(List.of(Role.BUY))
                .password(password)
                .user(user)
                .build();
    }

    private User createUser() {
        return User.builder()
                .sex(Sex.FEMALE)
                .nickname("꽃달린감나무")
                .cellPhone("01011112222")
                .birth(LocalDate.now())
                .profileImage("default.jpg")
                .name("꽃감이")
                .build();
    }

    private Password createPassword() {
        byte[] salt = passwordEncoder.getSalt();
        String encryptedPassword = passwordEncoder.encrypt("qwer1234", salt);

        return Password.builder()
                .password(encryptedPassword)
                .salt(salt)
                .build();
    }

}