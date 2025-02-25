package org.example.mollyapi.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.common.exception.CustomErrorResponse;
import org.example.mollyapi.order.dto.OrderCreateRequestDto;
import org.example.mollyapi.order.dto.OrderHistoryResponseDto;
import org.example.mollyapi.order.dto.OrderRequestDto;
import org.example.mollyapi.order.dto.OrderResponseDto;
import org.example.mollyapi.order.service.OrderService;
import org.example.mollyapi.user.auth.annotation.Auth;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @Auth
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody List<OrderRequestDto> orderRequests, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        OrderResponseDto response = orderService.createOrder(userId, orderRequests);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "주문 취소 API", description = "사용자의 요청으로 주문 프로세스를 종료")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId,
                                              @RequestParam(required = false, defaultValue = "false") boolean isExpired) {
        String message = orderService.cancelOrder(orderId, isExpired);
        return ResponseEntity.ok(message);
    }

    @Auth
    @GetMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderHistoryResponseDto> getUserOrders(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        OrderHistoryResponseDto orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    @Auth
    @GetMapping(value = "/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "주문 상세 조회 API", description = "주문 ID를 받아 주문의 orderDetail을 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "실패",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    public ResponseEntity<OrderResponseDto> getOrderDetails(@PathVariable Long orderId) {
        OrderResponseDto response = orderService.getOrderDetails(orderId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @Auth
    @PostMapping("/{orderId}/withdraw")
    @Operation(summary = "주문 철회 요청 API", description = "주문 ID를 받아 주문 철회 요청")
    public ResponseEntity<String> withdrawOrder(@PathVariable Long orderId) {
        log.info("주문 철회 요청: orderId={}", orderId);
        orderService.withdrawOrder(orderId);
        return ResponseEntity.ok("주문 철회가 완료되었습니다.");
    }
}