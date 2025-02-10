package org.example.mollyapi.user.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import org.example.mollyapi.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        Long authId = (Long)request.getAttribute("authId");
        String email = String.valueOf(request.getAttribute("email"));
        GetUserInfoResDto getUserInfoResDto = userService.getUserInfo(authId, email);
        return ResponseEntity.ok(getUserInfoResDto);
    }
}
