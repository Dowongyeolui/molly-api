package org.example.mollyapi.search.dto;



import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.util.List;

public record SearchItemResDto(
    List<ItemDto> item,
    Long nextCursorId,
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:SS", timezone = "Asia/Seoul")
    LocalDateTime nextLastCreatedAt,
    boolean isLastPage
){

}
