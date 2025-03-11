package org.example.mollyapi.user.auth.config;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class PasswordEncoderTest {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("소금은 항상 다른 값을 생성한다.")
    void getSalt() {
        //when
        byte[] salt1 = passwordEncoder.getSalt();
        byte[] salt2 = passwordEncoder.getSalt();

        //then
        assertThat(salt1).isNotEqualTo(salt2);
    }

    @Test
    @DisplayName("비밀번호를 암호화 한다")
    void encryptPassword() {
        //given
        String inputPassword = "qwer1234";
        byte[] salt = passwordEncoder.getSalt();

        //when
        String encrypted = passwordEncoder.encrypt(inputPassword, salt);

        //then
        assertThat(encrypted).isNotEqualTo(inputPassword);
    }

    @Test
    @DisplayName("입력된 비밀번호와 암호화 된 비밀번호 일치한다")
    void checkSuccess() {
        //given
        String inputPassword = "qwer1234";

        //when
        byte[] salt = passwordEncoder.getSalt();
        String originPassword = passwordEncoder.encrypt(inputPassword, salt);

       //then
        assertThat(passwordEncoder.check(originPassword, inputPassword, salt))
                .isTrue();

    }

    @Test
    @DisplayName("암호화 된 비밀번호의 다른 사용자의 비밀번호 입력 시 불일치한다")
    void checkFail() {
        //given
        String inputPassword = "qwer1234";
        String anotherInputPassword = "asdf5678";

        //when
        byte[] salt = passwordEncoder.getSalt();
        String originPassword = passwordEncoder.encrypt(inputPassword, salt);

        //then
        assertThat(passwordEncoder.check(anotherInputPassword, inputPassword, salt))
                .isFalse();

    }



}