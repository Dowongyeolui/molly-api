package org.example.mollyapi.user.auth.config;

import org.example.mollyapi.user.auth.entity.Auth;
import org.example.mollyapi.user.auth.entity.Password;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

@Component
public class PasswordEncoder {

    private final String encodeAlgorithm;
    private final String saltAlgorithm;
    private final int iterations;
    private final int keyLength;

    /***
     * 테스트 편리성을 위한 변수 주입
     * @param encodeAlgorithm 비밀번호 암호화 알고리즘
     * @param saltAlgorithm 소금 생성 알고리즘
     * @param iterations 해시 반봇
     * @param keyLength 해시 길이
     */
    public PasswordEncoder(
            @Value("${password.encode.algorithm}") String encodeAlgorithm,
            @Value("${password.encode.saltAlgorithm}") String saltAlgorithm,
            @Value("${password.encode.iterations}") int iterations,
            @Value("${password.encode.keyLength}") int keyLength
    ){
        this.encodeAlgorithm = encodeAlgorithm;
        this.iterations = iterations;
        this.keyLength = keyLength;
        this.saltAlgorithm = saltAlgorithm;
    }

    /***
     * 비밀번호 암호화
     * @param email 소금 해시를 얻기위한 값
     * @param password 아직 암호화되지 않은 비밀번호
     * @return 비밀번호 객체
     */
    public Password encrypt(String email, String password) {
        try {

            byte[] salt = getSalt(email);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), getSalt(email), iterations, keyLength);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(encodeAlgorithm);

            byte[] hash = factory.generateSecret(spec).getEncoded();
            String encodedPassword =  Base64.getEncoder().encodeToString(hash);
            return Password.of(encodedPassword, salt);

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

    }

    /***
     * 소금 생성 메소드
     * @param email 소금 생성시킬 변수
     * @return 소금
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    private byte[] getSalt(String email) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //임의 길이의 데이터를 고정 길이의 해시 값으로 변환하는 알고리즘 = MessageDigest
        MessageDigest messageDigest = MessageDigest.getInstance(saltAlgorithm);
        //이메일을 byte 값으로 변경
        byte[] keyBytes = email.getBytes(StandardCharsets.UTF_8);

        //saltAlgorithm 사용하여 해시, 결과를 바이드로 반환
        return  messageDigest.digest(keyBytes);
    }

    /***
     * 비밀번호 학인 메소드
     * @param auth 인증, 인가 정보
     * @param inputPassword 입력된 비밀번호
     * @return 비밀 번호 일치 여부
     */
    public boolean check(Auth auth, String inputPassword) {

        Password password = auth.getPassword();
        Password encryptPassword = encrypt(auth.getEmail(), inputPassword);

        return password.getPassword().equals(encryptPassword.getPassword());
    }
}
