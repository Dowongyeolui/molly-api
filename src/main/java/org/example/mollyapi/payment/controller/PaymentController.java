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
import org.example.mollyapi.payment.dto.request.PaymentInfoReqDto;
import org.example.mollyapi.payment.dto.response.PaymentInfoResDto;
import org.example.mollyapi.payment.dto.response.PaymentResDto;
import org.example.mollyapi.payment.entity.Payment;
import org.example.mollyapi.payment.service.PaymentService;
import org.example.mollyapi.payment.service.impl.PaymentServiceImpl;
import org.example.mollyapi.payment.util.MapperUtil;
import org.example.mollyapi.user.auth.annotation.Auth;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/payment")
@Tag(name = "결제 Controller", description = "결제 담당")
public class PaymentController {
    private final PaymentService paymentService;

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
                paymentConfirmReqDto.paymentType(),
                paymentConfirmReqDto.delivery()
        );

        return ResponseEntity.ok().body(PaymentResDto.from(payment));
    }


    @Operation(summary = "주문의 최근 결제내역 조회 api", description = "해당 주문의 가장 최근 결제 내역을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = CommonResDto.class))),
            @ApiResponse(responseCode = "400", description = "실패",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<PaymentInfoResDto> getPaymentInfo(HttpServletRequest request, @RequestBody PaymentInfoReqDto paymentInfoReqDto) {

        PaymentInfoResDto paymentInfoResDto = paymentService.findLatestPayment(paymentInfoReqDto.orderId());
        return ResponseEntity.ok().body(paymentInfoResDto);
    }

    @Operation(summary = "주문의 모든 결제내역 조회 api", description = "해당 주문의 모든 결제 내역을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = CommonResDto.class))),
            @ApiResponse(responseCode = "400", description = "실패",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    @PostMapping("/list")
    public ResponseEntity<List<PaymentInfoResDto>> getPaymentList(HttpServletRequest request, @RequestBody PaymentInfoReqDto paymentInfoReqDto) {

        List<PaymentInfoResDto> paymentInfoResDtos = paymentService.findAllPayments(paymentInfoReqDto.orderId());
        return ResponseEntity.ok().body(paymentInfoResDtos);
    }

    @Operation(summary = "나의 모든 결제내역 조회 api", description = "나의 모든 결제 내역을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = CommonResDto.class))),
            @ApiResponse(responseCode = "400", description = "실패",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    @GetMapping("/my")
    @Auth
    public ResponseEntity<List<PaymentInfoResDto>> getMyPaymentList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<PaymentInfoResDto> paymentInfoResDtos = paymentService.findUserPayments(userId);
        return ResponseEntity.ok().body(paymentInfoResDtos);
    }

    @Operation(summary = "주문 생성후 결제 성공과 주문 성공 테스트 엔드포인트", description = "paymentKey, paymentType, delivery 필드는 임의의 값을 대입해 결제 정보를 생성하고 주문 성공 로직을 실행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = CommonResDto.class))),
            @ApiResponse(responseCode = "400", description = "실패",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class)))
    })

    @PostMapping("/success/test")
    public ResponseEntity<Void> paymentSuccessProcessTest(HttpServletRequest request, @RequestBody PaymentConfirmReqDto paymentConfirmReqDto) {
//        Long userId = (Long) request.getAttribute("userId");
        log.warn(paymentConfirmReqDto.toString());
        Payment payment = paymentService.createPayment(19L,
                paymentConfirmReqDto.orderId(),
                paymentConfirmReqDto.tossOrderId(),
                paymentConfirmReqDto.paymentKey(),
                paymentConfirmReqDto.paymentType(),
                paymentConfirmReqDto.amount());

        log.info(MapperUtil.convertDtoToJson(paymentConfirmReqDto.delivery()));
        log.warn("yes");

        paymentService.successPayment(payment,
                paymentConfirmReqDto.tossOrderId(),
                paymentConfirmReqDto.point(),
                MapperUtil.convertDtoToJson(paymentConfirmReqDto.delivery())
                );



        return ResponseEntity.ok().build();
    }

}
