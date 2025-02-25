package org.example.mollyapi.order.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DirectOrderRequestDto {
    @NotNull
    private Long itemId;

    @NotNull
    private Long quantity;

    @JsonCreator
    public DirectOrderRequestDto(
            @JsonProperty("itemId") Long itemId,
            @JsonProperty("quantity") Long quantity) {
        if (itemId == null || quantity == null) {
            throw new IllegalArgumentException("바로 주문 시 itemId와 quantity는 필수입니다.");
        }
        this.itemId = itemId;
        this.quantity = quantity;
    }
}