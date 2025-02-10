package org.example.mollyapi.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.example.mollyapi.user.auth.entity.Auth;
import org.example.mollyapi.user.auth.entity.Password;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.type.Role;
import org.example.mollyapi.user.type.Sex;

import java.time.LocalDate;
import java.util.List;

public record SignUpReqDto(

        @NotBlank(message = "적당한 닉네임이 생각나지 않으면, 하늘을나는 고양이는 어때요?")
        String nickname,

        @Schema( description = "'-'를 제외하고 보내주세요, 10~11자리 숫자만 가능합니다", example = "cats@cats.com")
        @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10~11자리 숫자만 가능합니다.")
        String cellPhone,

        @Schema( description = "MAIL, FEMALE 만 가능합니다. 소문자도 가능해요", example = "MALE, FEMALE")
        @NotNull(message = "유효하지 않은 성별이 입력되었습니다.")
        Sex sex,

        @Schema(description = "yyyy-MM-dd 이 형식에 맞춰서 보내주세요", example = "2025-03-02")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        LocalDate birth,

        @NotBlank(message = "이름은 필 수 값입니다.")
        String name,

        @Schema(description = "이메일 형식에 맞춰보내주세요", example = "cats@cats.com")
        @Email(message = "이메일 형식을 맞춰주세요! cat@cats.com")
        String email,

        @Schema(description = "비밀번호는 영어 소문자, 숫자, 특수 문자를 모두 포함하고 8자 이상이어야 합니다.", example = "cats123456@")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\",\\\\|,.<>\\/?]).{8,}$",
                message = "비밀번호는 영어 소문자, 숫자, 특수 문자를 모두 포함하고 8자 이상이어야 합니다.")
        String password,

        @Schema(description = "seller = true,  buyer = false", example = "true, false")
        Boolean isSeller
) {
        /***
         * Dto -> User 전환
         * @return User
         */

        public User toUser(Auth auth){

                return User.builder()
                        .nickname(nickname)
                        .cellPhone(cellPhone)
                        .birth(birth)
                        .name(name)
                        .flag(false)
                        .point(0)
                        .profileImage("Default Profile Image")
                        .sex(sex)
                        .auth(auth)
                        .build();
        }

        /***
         * Dto -> Auth 전환
         * @param password 암호화된 비밀번호
         * @return Auth
         */
        public Auth toAuth( Password password){

                return Auth.builder()
                        .email(email)
                        .role(isSeller ? List.of(Role.BUY, Role.SELL) : List.of(Role.BUY))
                        .password(password)
                        .build();
        }
}
