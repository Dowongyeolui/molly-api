package org.example.mollyapi.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.mollyapi.common.exception.CustomErrorResponse;
import org.example.mollyapi.user.auth.annotation.Auth;
import org.example.mollyapi.user.dto.GetUserInfoResDto;
import org.example.mollyapi.user.dto.GetUserSummaryInfoWithPointResDto;
import org.example.mollyapi.user.dto.UpdateUserReqDto;
import org.example.mollyapi.user.dto.UpdateUserResDto;
import org.example.mollyapi.user.service.UserService;
import org.springframework.http.HttpStatusCode;
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
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(userService.getUserInfo(userId));
    }

    @Auth
    @PutMapping()
    @Operation(summary = "사용자 정보 수정", description = "변경된 값이 없으면, 204 반환, 있으면 201 반환")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "성공",
                    content = @Content(schema = @Schema(implementation = UpdateUserResDto.class))),
            @ApiResponse(responseCode = "204", description = "성공"),
            @ApiResponse(responseCode = "400", description = "실패",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class)))})
    public ResponseEntity<?> updateUserInfo(
            @Valid @RequestBody UpdateUserReqDto updateUserReqDto,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.updateUserInfo(updateUserReqDto, userId);
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
            @Parameter(description = "true = 포인트 같이 전달, false = 포인트 제외", required = true)
            @RequestParam(name = "include-point")
            boolean includePoint,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        var result = includePoint
                ? userService.getUserSummaryWithPoint(userId)
                : userService.getUserSummaryInfo(userId);

        return ResponseEntity.ok(result);
    }


    @Auth
    @Operation(summary = "사용자 정보 삭제", description = "사용자 정보 삭제 controller")
    @DeleteMapping("")
    @ApiResponse(responseCode = "204", description = "성공")
    public ResponseEntity<?> deleteUserInfo(HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        userService.deleteUserInfo(userId);
        return ResponseEntity.status(HttpStatusCode.valueOf(204)).build();
    }
}
