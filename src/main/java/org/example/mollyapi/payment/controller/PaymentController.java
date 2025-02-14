package org.example.mollyapi.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.common.dto.CommonResDto;
import org.example.mollyapi.common.exception.CustomErrorResponse;
import org.example.mollyapi.payment.dto.request.PaymentConfirmReqDto;
import org.example.mollyapi.payment.dto.response.PaymentResDto;
import org.example.mollyapi.payment.entity.Payment;
import org.example.mollyapi.payment.service.impl.PaymentServiceImpl;
import org.example.mollyapi.user.auth.annotation.Auth;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/payment")
@Tag(name = "결제 Controller", description = "결제 담당")
public class PaymentController {
    private final PaymentServiceImpl paymentService;

    @PostMapping("/confirm")
    @Operation(summary = "결제 검증 api", description = "tossPayments 결제 승인을 요청하고, 결제 금액을 검증합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = CommonResDto.class))),
            @ApiResponse(responseCode = "400", description = "실패",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    @Auth
    public ResponseEntity<PaymentResDto> confirmPayment(HttpServletRequest request, @RequestBody PaymentConfirmReqDto paymentConfirmReqDto) {

        Long userId = (Long) request.getAttribute("userId");

        Payment payment = paymentService.processPayment(
                userId,
                paymentConfirmReqDto.paymentKey(),
                paymentConfirmReqDto.tossOrderId(),
                paymentConfirmReqDto.amount(),
                paymentConfirmReqDto.point(),
                paymentConfirmReqDto.deliveryId(),
                paymentConfirmReqDto.paymentType()
        );

        return ResponseEntity.ok().body(PaymentResDto.from(payment));
    }
}
