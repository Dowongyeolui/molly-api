package org.example.mollyapi.user.repository;

import org.example.mollyapi.user.auth.config.PasswordEncoder;
import org.example.mollyapi.user.auth.entity.Auth;
import org.example.mollyapi.user.auth.entity.Password;
import org.example.mollyapi.user.auth.repository.AuthRepository;
import org.example.mollyapi.user.dto.GetUserInfoResDto;
import org.example.mollyapi.user.dto.GetUserSummaryInfoWithPointResDto;
import org.example.mollyapi.user.entity.User;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;


@ActiveProfiles("test")
@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthRepository authRepository;

    @Test
    @DisplayName("닉네임으로 사용자를 조회할 수 있다.")
    void existsByNickname_Success() {

        //given
        User user = createUser();

        userRepository.save(user);
        //when
        boolean exists = userRepository.existsByNickname("꽃달린감나무");

        //then
        assertTrue(exists);
    }

    @Test
    @DisplayName("닉네임으로 회원가입 하지 않은 사용자를 조회하면 실패한다 ")
    void existsByNickname_Fail() {

        //given
        User user = createUser();

        //when
        boolean exists = userRepository.existsByNickname("꽃달린감나무");

        //then
        assertFalse(exists);
    }

    @Test
    @DisplayName("userId를 통해 사용자 정보를 조회할 수 있다.")
    void getUserInfo_Success() {
        //given
        User givenUser = userRepository.save(createUser());
        Password password = createPassword();
        authRepository.save(createAuth(password, givenUser));

        //when
        Optional<GetUserInfoResDto> userInfo = userRepository.getUserInfo(givenUser.getUserId());

        //then
        assertThat(userInfo)
                .isPresent()
                .get()
                .extracting("nickname", "name", "cellPhone")
                .contains("꽃달린감나무", "꽃감이", "01011112222");
    }

    @Test
    @DisplayName("user_id = null 인 사용자 정보를 조회는 Null 이다.")
    void getUserInfo_UserId_Null() {

        //when //then
        Optional<GetUserInfoResDto> userInfo = userRepository.getUserInfo(null);

        assertThat(userInfo.isPresent()).isFalse();
    }

    @Test
    @DisplayName("저장 되지않은 사용자 정보를 조회는 Null을 반환한다")
    void getUserInfo_Fail() {
        //given
        //when //then
        Optional<GetUserInfoResDto> userInfo = userRepository.getUserInfo(7L);

        assertThat(userInfo.isPresent()).isFalse();
    }

    @Test
    @DisplayName("userId를 통해 사용자의 요약 정보를 조회할 수 있다.")
    void getUserSummaryInfo_Success() {
        //given
        User givenUser = userRepository.save(createUser());
        Password password = createPassword();
        authRepository.save(createAuth(password, givenUser));

        //when
        Optional<GetUserSummaryInfoWithPointResDto> userSummaryInfo
                = userRepository.getUserSummaryInfo(givenUser.getUserId());

        //then
        assertThat(userSummaryInfo)
                .isPresent()
                .get()
                .extracting("name", "point", "email")
                .contains("꽃감이", 0, "test@example.com");
    }

    @Test
    @DisplayName("userId = null 이면 사용자의 요약 정보는 null 이다.")
    void getUserSummaryInfo_UserId_Null() {

        //when
        Optional<GetUserSummaryInfoWithPointResDto> userSummaryInfo
                = userRepository.getUserSummaryInfo(null);

        //then
        assertThat(userSummaryInfo.isPresent()).isFalse();
    }

    @Test
    @DisplayName("없는 userId 이면 사용자의 요약 정보는 null 이다.")
    void getUserSummaryInfo_No_UserId() {

        //when
        Optional<GetUserSummaryInfoWithPointResDto> userSummaryInfo
                = userRepository.getUserSummaryInfo(11L);

        //then
        assertThat(userSummaryInfo.isPresent()).isFalse();
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