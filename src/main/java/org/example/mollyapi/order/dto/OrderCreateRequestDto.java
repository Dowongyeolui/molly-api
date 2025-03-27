package org.example.mollyapi.order.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OrderCreateRequestDto(
        List<OrderRequestDto> orderRequests
) {
    @JsonCreator
    public OrderCreateRequestDto(@JsonProperty("orderRequests") List<OrderRequestDto> orderRequests) {
        this.orderRequests = orderRequests;
    }
}

