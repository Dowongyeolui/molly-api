package org.example.mollyapi.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.example.mollyapi.user.type.Sex;

import java.time.LocalDate;

public record UpdateUserReqDto(
        @Schema(description = "이름이 마음에 들지 않으세요? X-3596은 어때요?", example = "X-3456")
        String name,

        @Schema(description = "적당한 닉네임이 생각나지 않으면, 물구나무 서있는 고양이는 어때요?", example = "물구나무 서있는 고양이")
        String nickname,

        @Schema( description = "'-'를 제외하고 보내주세요, 10~11자리 숫자만 가능합니다", example = "01773229999")
        @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10~11자리 숫자만 가능합니다.")
        String cellPhone,

        @Schema( description = "MAIL, FEMALE 만 가능합니다. 소문자도 가능해요", example = "MALE")
        @NotNull(message = "유효하지 않은 성별이 입력되었습니다.")
        Sex sex,

        @JsonFormat(shape = JsonFormat.Shape.STRING,  pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        LocalDate birth
) {
}
