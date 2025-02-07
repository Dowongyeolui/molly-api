package org.example.mollyapi.user.config;

import org.example.mollyapi.user.entity.Auth;
import org.example.mollyapi.user.entity.Password;
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

    private byte[] getSalt(String email) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //임의 길이의 데이터를 고정 길이의 해시 값으로 변환하는 알고리즘 = MessageDigest
        MessageDigest messageDigest = MessageDigest.getInstance(saltAlgorithm);
        //이메일을 byte 값으로 변경
        byte[] keyBytes = email.getBytes(StandardCharsets.UTF_8);

        //saltAlgorithm 사용하여 해시, 결과를 바이드로 반환
        return  messageDigest.digest(keyBytes);
    }

    public boolean check(Auth auth, String inputPassword) {

        Password password = auth.getPassword();
        Password encryptPassword = encrypt(auth.getEmail(), inputPassword);

        return password.getPassword().equals(encryptPassword.getPassword());
    }
}
