package org.example.mollyapi.user.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.mollyapi.common.exception.CustomErrorResponse;
import org.example.mollyapi.user.auth.dto.SignInReqDto;
import org.example.mollyapi.user.auth.service.SignInService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Tag(name = "로그인 Controller", description = "로그인 (_''_)")
@RestController
@RequiredArgsConstructor
public class SignInController {

    private final SignInService signInService;
    private static final String HEADER_STRING = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @PostMapping("/sign-in")
    @Operation( summary = "로그인", description = "사용자는 로그인을 할 수 있습니다. 토큰은 Header 를 참고해주세요")
    @ApiResponses({
            @ApiResponse( responseCode = "400", description = "비밀번호 불일치, 존재하지 않는 회원",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class))
            ),
            @ApiResponse( responseCode = "204", description = "로그인 성공, 토큰은 Header 를 참고해주세요")
    })

    public ResponseEntity<?> signIn(@RequestBody SignInReqDto signInReqDto, HttpServletResponse response) {
        String token = signInService.signIn(signInReqDto);
        token = URLEncoder.encode(TOKEN_PREFIX + token, StandardCharsets.UTF_8);
        Cookie httpOnlyCookie = new Cookie(HEADER_STRING, token);
        httpOnlyCookie.setHttpOnly(true); // HttpOnly 설정
//        httpOnlyCookie.setSecure(true); // HTTPS에서만 전송 (운영 환경에서는 true로 설정해야 함)
        httpOnlyCookie.setPath("/"); // 쿠키의 유효 경로 (루트 경로로 설정)
        httpOnlyCookie.setMaxAge(3600); // 쿠키 유효 시간 (초 단위, 예: 1시간)

        response.addCookie(httpOnlyCookie);

        return ResponseEntity.noContent().build();
    }
}
