package org.example.mollyapi.order.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OrderRequestWrapper {
    private List<CartOrderRequestDto> cartOrderRequests;
    private DirectOrderRequestDto directOrderRequest;

    @JsonCreator
    public OrderRequestWrapper(
            @JsonProperty("cartOrderRequests") List<CartOrderRequestDto> cartOrderRequests,
            @JsonProperty("directOrderRequest") DirectOrderRequestDto directOrderRequest) {

        // 장바구니 주문과 바로 주문을 동시에 받을 수 없음
        if ((cartOrderRequests != null && !cartOrderRequests.isEmpty()) && directOrderRequest != null) {
            throw new IllegalArgumentException("장바구니 주문(cartOrderRequests)과 바로 주문(directOrderRequest)을 동시에 보낼 수 없습니다.");
        }

        this.cartOrderRequests = cartOrderRequests;
        this.directOrderRequest = directOrderRequest;
    }
}