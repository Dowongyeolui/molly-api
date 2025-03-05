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
import org.example.mollyapi.order.dto.*;
import org.example.mollyapi.order.service.OrderServiceImpl;
import org.example.mollyapi.payment.dto.response.PaymentResDto;
import org.example.mollyapi.user.auth.annotation.Auth;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderServiceImpl orderServiceImpl;

    /**
     * 주문 생성
     */
    @Auth
    @PostMapping(produces = "application/json")
    @Operation(summary = "주문 생성 API", description = "사용자가 장바구니에서 또는 상품페이지에서 주문을 요청")
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody OrderCreateRequestDto orderRequestDto,
            HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");

        OrderResponseDto response = orderServiceImpl.createOrder(userId, orderRequestDto.orderRequests());

        return ResponseEntity.ok(response);
    }

    /**
     * 주문 취소
     */
    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "주문 취소 API", description = "사용자의 요청으로 주문 프로세스를 종료")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId) {
        orderServiceImpl.cancelOrder(orderId);
        return ResponseEntity.ok("주문이 취소되었습니다.");
    }

    /**
     * 사용자의 주문 내역 조회
     */
    @Auth
    @GetMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "사용자 주문 내역 조회 API", description = "사용자의 주문 목록을 조회")
    public ResponseEntity<OrderHistoryResponseDto> getUserOrders(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        OrderHistoryResponseDto orders = orderServiceImpl.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * 주문 상세 조회
     */
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
        OrderResponseDto response = orderServiceImpl.getOrderDetails(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * 주문 철회 요청
     */
    @Auth
    @PostMapping("/{orderId}/withdraw")
    @Operation(summary = "주문 철회 요청 API", description = "주문 ID를 받아 주문 철회 요청")
    public ResponseEntity<String> withdrawOrder(@PathVariable Long orderId) {
        log.info("주문 철회 요청: orderId={}", orderId);
        orderServiceImpl.withdrawOrder(orderId);
        return ResponseEntity.ok("주문 철회가 완료되었습니다.");
    }

    /**
     * 주문 결제 요청
     */
    @Auth
    @PostMapping("/{orderId}/payment")
    @Operation(summary = "주문 결제 요청 API", description = "주문에 대한 결제 요청 및 성공/실패 처리")
    public ResponseEntity<PaymentResDto> processPayment(
            HttpServletRequest request,
            @PathVariable Long orderId,
            @Valid @RequestBody OrderConfirmRequestDto orderConfirmRequestDto) {

        Long userId = (Long) request.getAttribute("userId");

        PaymentResDto response = orderServiceImpl.processPayment(
                userId,
                orderConfirmRequestDto.paymentKey(),
                orderConfirmRequestDto.tossOrderId(),
                orderConfirmRequestDto.amount(),
                orderConfirmRequestDto.point(),
                orderConfirmRequestDto.paymentType(),
                orderConfirmRequestDto.delivery()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 결제 재시도 API. (자동 재시도 3회 실패 후) 사용자 API를 받아 수동 재시도
     */
    @Auth
    @PostMapping("/{orderId}/retry-payment")
    @Operation(summary = "결제 재시도 API", description = "결제 실패 시 사용자가 재시도를 요청합니다.")
    public ResponseEntity<String> retryPayment(
            HttpServletRequest request,
            @PathVariable Long orderId) {

        Long userId = (Long) request.getAttribute("userId");
        orderServiceImpl.retryPayment(userId, orderId);
        return ResponseEntity.ok("결제 재시도가 진행되었습니다.");
    }

    /**
     * 반품 확인 API
     */
    @Auth
    @PostMapping("/{orderId}/return-confirm")
    @Operation(summary = "반품 확인 API", description = "반품 도착 확인, 이후 자동 환불 진행 (관리자용)")
    public ResponseEntity<String> confirmReturn(@PathVariable Long orderId) {
        log.info("반품 확인 요청: orderId={}", orderId);
        orderServiceImpl.handleReturnArrived(orderId);
        return ResponseEntity.ok("반품이 확인되어 환불 처리가 시작됩니다.");
    }
}