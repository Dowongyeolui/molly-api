package org.example.mollyapi.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.example.mollyapi.user.type.Sex;

import java.time.LocalDate;

public record UpdateUserResDto(

        String name,

        String nickname,

        String cellPhone,

        Sex sex,

        @JsonFormat(shape = JsonFormat.Shape.STRING,  pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        LocalDate birth
){
}
