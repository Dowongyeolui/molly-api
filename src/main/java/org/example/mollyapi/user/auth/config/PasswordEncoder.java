package org.example.mollyapi.user.auth.config;

import org.example.mollyapi.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.UUID;

import static org.example.mollyapi.common.exception.error.impl.AuthError.RETRY_ACTIVE;

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
     * @param inputPassword 아직 암호화되지 않은 비밀번호
     * @return 비밀번호 객체
     */
    public String encrypt(String inputPassword, byte[] salt) {
        try {

            KeySpec spec = new PBEKeySpec(inputPassword.toCharArray(), salt, iterations, keyLength);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(encodeAlgorithm);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new CustomException(RETRY_ACTIVE);
        }

    }

    /***
     * 소금 생성 메소드
     * @return 소금
     */
    public byte[] getSalt() {

        try {
            //임의 길이의 데이터를 고정 길이의 해시 값으로 변환하는 알고리즘 = MessageDigest
            MessageDigest messageDigest = MessageDigest.getInstance(saltAlgorithm);
            byte[] keyBytes = UUID.randomUUID()
                    .toString()
                    .replace("_", "")
                    .getBytes(StandardCharsets.UTF_8);

            //saltAlgorithm 사용하여 해시, 결과를 바이드로 반환
            return  messageDigest.digest(keyBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new CustomException(RETRY_ACTIVE);
        }
    }

    /***
     * 비밀번호 학인 메소드
     * @param inputPassword 입력된 비밀번호
     * @return 비밀 번호 일치 여부
     */
    public boolean check( String originPassword, String inputPassword, byte[] salt) {
        return encrypt(inputPassword, salt).equals(originPassword);
    }
}
