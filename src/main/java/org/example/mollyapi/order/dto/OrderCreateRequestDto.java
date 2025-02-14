package org.example.mollyapi.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OrderCreateRequestDto {
    private Long userId;
    private List<OrderRequestDto> orderRequests;

    public OrderCreateRequestDto(Long userId, List<OrderRequestDto> orderRequests) {
        this.userId = userId;
        this.orderRequests = orderRequests;
    }
}
