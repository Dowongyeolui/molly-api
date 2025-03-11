package org.example.mollyapi.user.service;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.user.auth.config.PasswordEncoder;
import org.example.mollyapi.user.auth.entity.Auth;
import org.example.mollyapi.user.auth.entity.Password;
import org.example.mollyapi.user.auth.repository.AuthRepository;
import org.example.mollyapi.user.dto.GetUserInfoResDto;
import org.example.mollyapi.user.dto.GetUserSummaryInfoResDto;
import org.example.mollyapi.user.dto.GetUserSummaryInfoWithPointResDto;
import org.example.mollyapi.user.dto.UpdateUserReqDto;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
class UserServiceTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("사용자 정보를 조회할 수 있다.")
    void getUserInfo_Success() {
        //given
        User givenUser = createUser();
        User savedUser = userRepository.save(givenUser);

        Password givenPassword = createPassword();
        Auth auth = createAuth(givenPassword, savedUser);

        authRepository.save(auth);

        //when
        GetUserInfoResDto result = userService.getUserInfo(savedUser.getUserId());

        //then
        assertThat(result)
                .extracting("nickname", "name", "email")
                .contains("꽃달린감나무", "꽃감이", "test@example.com");
    }

    @Test
    @DisplayName("user_id가 유효하지 않으면 오류가 생긴다.")
    void getUserInfo_Wrong_User_Id() {
        //given
        User givenUser = createUser();
        User savedUser = userRepository.save(givenUser);

        Password givenPassword = createPassword();
        Auth auth = createAuth(givenPassword, savedUser);

        authRepository.save(auth);

        //when, then
        assertThatThrownBy(() -> userService.getUserInfo(7L))
                .isInstanceOf(CustomException.class)
                .hasMessage("없는 사용자 입니다.");
    }


    @Test
    @DisplayName("user_id를 통해, 사용자의 요약 정보와 포인트를 조회할 수 있다.")
    void getUserSummaryWithPoint_Success() {
        //given
        User givenUser = createUser();
        User savedUser = userRepository.save(givenUser);

        Password givenPassword = createPassword();
        Auth auth = createAuth(givenPassword, savedUser);

        authRepository.save(auth);

        //when
        GetUserSummaryInfoWithPointResDto result = userService.getUserSummaryWithPoint(savedUser.getUserId());

        //then
        assertThat(result)
                .extracting("point", "name", "email")
                .contains(0, "꽃감이", "test@example.com");
    }

    @Test
    @DisplayName("유효하지 않은 user_id를 통해, 사용자의 요약 정보와 포인트를 조회할 수 없다.")
    void getUserSummaryWithPoint_Wrong_UserId() {
        //given
        User givenUser = createUser();
        User savedUser = userRepository.save(givenUser);

        Password givenPassword = createPassword();
        Auth auth = createAuth(givenPassword, savedUser);

        authRepository.save(auth);

        //when
        assertThatThrownBy(() -> userService.getUserSummaryWithPoint(7L))
                .isInstanceOf(CustomException.class)
                .hasMessage("없는 사용자 입니다.");
    }


    @Test
    @DisplayName("사용자의 요약 정보를 조회할 수 있다.")
    void getUserSummaryInfo_Success() {

        //given
        User givenUser = createUser();
        User savedUser = userRepository.save(givenUser);

        Password givenPassword = createPassword();
        Auth auth = createAuth(givenPassword, savedUser);

        authRepository.save(auth);

        //then //when
        GetUserSummaryInfoResDto result = userService.getUserSummaryInfo(savedUser.getUserId());

        assertThat(result).extracting("name", "email")
                .contains("꽃감이", "test@example.com");
    }

    @Test
    @DisplayName("유효한 user_id로 사용자의 요약 정보를 조회하면 실패한다..")
    void getUserSummaryInfo() {

        //given
        User givenUser = createUser();
        User savedUser = userRepository.save(givenUser);

        Password givenPassword = createPassword();
        Auth auth = createAuth(givenPassword, savedUser);

        authRepository.save(auth);

        //then //when
        assertThatThrownBy(() -> userService.getUserSummaryInfo(7L))
                .isInstanceOf(CustomException.class)
                .hasMessage("없는 사용자 입니다.");
    }

    @Test
    @DisplayName("사용자 정보를 수정할 수 있다.")
    void updateUserInfo() {
        //given
        User givenUser = createUser();
        User savedUser = userRepository.save(givenUser);

        Password givenPassword = createPassword();
        Auth auth = createAuth(givenPassword, savedUser);

        authRepository.save(auth);

        LocalDate updatedBirth = LocalDate.of(1999, 9, 22);
        UpdateUserReqDto updateUserReqDto = new UpdateUserReqDto(
                "귤감이",
                "귤달린감나무",
                null,
                updatedBirth
        );

        //when
        userService.updateUserInfo(updateUserReqDto, savedUser.getUserId());
        Optional<User> result = userRepository.findById(savedUser.getUserId());
        //then
        assertThat(result).isPresent()
                .get()
                .extracting("name", "nickname", "cellPhone", "birth")
                .contains("귤감이", "귤달린감나무", "01011112222", updatedBirth);
    }

    @Test
    @DisplayName("사용자의 Flag 를 활성화해 삭제를 원하는 사용자임을 나타낼 수 있다.")
    void deleteUserInfo_Success() {
        //given
        User givenUser = createUser();
        User savedUser = userRepository.save(givenUser);

        Password givenPassword = createPassword();
        Auth auth = createAuth(givenPassword, savedUser);

        authRepository.save(auth);

        //when
        userService.deleteUserInfo(givenUser.getUserId());

        Optional<User> result = userRepository.findById(savedUser.getUserId());


        //then
        assertThat(result).isPresent();
        assertThat(result.get().getFlag()).isEqualTo(true);
    }
    @Test
    @DisplayName("잘못된 user_id로 사용자의 Flag 를 활성화에 실패한다.")
    void deleteUserInfo_Wrong_userId() {
        //given
        User givenUser = createUser();
        User savedUser = userRepository.save(givenUser);

        Password givenPassword = createPassword();
        Auth auth = createAuth(givenPassword, savedUser);

        authRepository.save(auth);

        //when
        assertThatThrownBy( () -> userService.deleteUserInfo(7L))
                .isInstanceOf(CustomException.class)
                .hasMessage("없는 사용자 입니다.");

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