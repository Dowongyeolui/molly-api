package org.example.mollyapi.user.auth.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.user.type.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static org.example.mollyapi.common.exception.error.impl.AuthError.WRONG_APPROACH;

@Component
public class Jwt {

    private final String SECRET_KET;
    private final long EXPIRATION_TIME;

    /*
    요렇게 생성자 주입을 하는 이유는 테스트 코드를 작성하기 용이해져
     */
    public Jwt(
            @Value("${jjwt.secret-key}") String secretKey,
            @Value("${jjwt.expiration-time}") long expirationTime) {

        this.SECRET_KET = secretKey;
        this.EXPIRATION_TIME = expirationTime;
    }

    public String generateToken(Long authId, String email, List<Role> roles) {
        return Jwts.builder()
                .setSubject(email)
                .claim("authId", authId)
                .claim("email", email)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KET)
                .compact();
    }

    public String extractMemberEmail(String token) {
        return getClaims(token).getSubject();
    }

    public Long extractMemberId(String token) {
        return getClaims(token).get("authId", Long.class);
    }

    public Date extractExpiration(String token) {
        return getClaims(token).getExpiration();
    }

    public List<?> extractRole(String token) {
        return getClaims(token).get("roles", List.class);
    }

    private Claims getClaims(String token) {
        try{
            return Jwts.parser()
                    .setSigningKey(SECRET_KET)
                    .parseClaimsJws(token)
                    .getBody();

        } catch (Exception e) {
            throw new CustomException(WRONG_APPROACH);
        }

    }

}
