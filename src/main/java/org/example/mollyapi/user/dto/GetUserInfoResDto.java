package org.example.mollyapi.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record GetUserInfoResDto(
        String profileImage,
        String nickname,
        String name,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        LocalDate birth,
        String cellPhone,
        String email
) {
}
