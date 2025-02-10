package org.example.mollyapi.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.mollyapi.common.exception.CustomErrorResponse;
import org.example.mollyapi.user.auth.annotation.Auth;
import org.example.mollyapi.user.dto.GetUserInfoResDto;
import org.example.mollyapi.user.dto.GetUserSummaryInfoWithPointResDto;
import org.example.mollyapi.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Controller", description = "유저 관련 엔드포인트")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Auth
    @GetMapping("/info")
    @Operation(summary = "유저 프로필 수정 시 필요한 데이터 조회", description = "유저가 데이터를 수정하기 전에 표시할 데이터 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetUserInfoResDto.class))),
            @ApiResponse(responseCode = "400", description = "실패",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    public ResponseEntity<GetUserInfoResDto> getUserInfo(HttpServletRequest request) {
        Long authId = (Long) request.getAttribute("authId");
        return ResponseEntity.ok(userService.getUserInfo(authId));
    }

    @Auth
    @GetMapping("/info/summary")
    @Operation(summary = "유저 요약 프로필 데이터 조회", description = "유저 요약 프로필 데이터 조회 with 포인트")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetUserSummaryInfoWithPointResDto.class))),
            @ApiResponse(responseCode = "400", description = "실패",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    public ResponseEntity<?> getUserSummaryInfoWithOptionalPoint(
            @Parameter(description = "true = 포인트 같이 전달, false = 포인트 제외", required = true) @RequestParam(name = "include-point") boolean includePoint,
            HttpServletRequest request) {
        Long authId = (Long) request.getAttribute("authId");

        var result = includePoint
                ? userService.getUserSummaryWithPoint(authId)
                : userService.getUserSummaryInfo(authId);

        return ResponseEntity.ok(result);
    }
}
