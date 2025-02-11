package org.example.mollyapi.user.auth.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.user.auth.config.Jwt;
import org.springframework.stereotype.Component;

import java.util.Date;

import static org.example.mollyapi.common.exception.error.impl.AuthError.*;

@Aspect
@Component
public class AuthAspect {

    private final Jwt jwt;
    private final HttpServletRequest request;

    private static final String HEADER_STRING = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer";

    public AuthAspect(Jwt jwt, HttpServletRequest request) {
        this.jwt = jwt;
        this.request = request;
    }

    @Before("@annotation(org.example.mollyapi.user.auth.annotation.Auth)")
    public void authenticateJwt(){

        String header = request.getHeader(HEADER_STRING);

        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            throw new CustomException(WRONG_APPROACH);
        }

        String token = header.substring(TOKEN_PREFIX.length());
        validateToken(token);

        request.setAttribute("email", jwt.extractMemberEmail(token));

        request.setAttribute("authId", jwt.extractAuthId(token));

        request.setAttribute("role", jwt.extractRole(token));

        request.setAttribute("userId", jwt.extractUserId(token));
    }

    private void validateToken(String token) {

        //만료 여부 확인
        Date expiration = jwt.extractExpiration(token);

        if(expiration == null || expiration.before(new Date())) {
            throw new CustomException(WRONG_APPROACH);
        }


    }
}
