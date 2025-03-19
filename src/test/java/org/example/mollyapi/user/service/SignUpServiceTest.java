package org.example.mollyapi.user.service;

import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.groups.Tuple;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.user.auth.entity.Auth;
import org.example.mollyapi.user.auth.entity.Password;
import org.example.mollyapi.user.auth.repository.AuthRepository;
import org.example.mollyapi.user.dto.SignUpReqDto;
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
import static org.assertj.core.groups.Tuple.tuple;
import static org.example.mollyapi.user.type.Role.BUY;
import static org.example.mollyapi.user.type.Role.SELL;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class SignUpServiceTest {

    @Autowired
    private SignUpService signUpService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthRepository authRepository;

    @Test
    @DisplayName("필수 값을 입력하면, 회원가입을 할 수 있다. 첫 회원가입 시 Point가 0이다.")
    void signUp_Success() {
        //given
        SignUpReqDto request = createRequest("꽃달린감나무", "test@example.com");

        signUpService.signUp(request);
        //when

        Optional<Auth> auth = authRepository.findByEmail("test@example.com");
        Optional<User> user = userRepository.findById(auth.get().getAuthId());

        //then
        assertThat(auth)
                .isPresent()
                .get()
                .extracting("email")
                .isEqualTo("test@example.com");

        assertThat(auth.get().getRole()).contains(BUY, SELL);

        assertThat(user).isPresent().get()
                .extracting("nickname","point")
                .contains("꽃달린감나무", 0);
    }

    @Test
    @DisplayName("중복된 이메일로 회원가입시, 회원가입에 실패한다.")
    void signUp_Duplicate_Email() {
        //given
        SignUpReqDto request1 = createRequest("꽃달린감나무", "test@example.com");
        SignUpReqDto request2 = createRequest("총달린귤나무", "test@example.com");

        signUpService.signUp(request1);

        //when //then
        assertThatThrownBy(() -> signUpService.signUp(request2))
                .isInstanceOf(CustomException.class)
                .hasMessage("이미 존재하는 회원입니다.");
    }

    @Test
    @DisplayName("중복된 닉네임으로 회원가입시, 회원가입에 실패한다.")
    void signUp_Duplicate_Nickname() {
        //given
        SignUpReqDto request1 = createRequest("꽃달린감나무", "test@example.com");
        SignUpReqDto request2 = createRequest("꽃달린감나무", "other@example.com");

        signUpService.signUp(request1);

        //when //then
        assertThatThrownBy(() -> signUpService.signUp(request2))
                .isInstanceOf(CustomException.class)
                .hasMessage("중복되는 닉네임입니다.");
    }


    private static SignUpReqDto createRequest(String nickName, String email) {
        return new SignUpReqDto(
                nickName,
                "01011112222",
                Sex.MALE,
                LocalDate.of(1997, 5, 12),
                "꽃감이",
                email,
                "qwer1234@",
                true
        );
    }


}