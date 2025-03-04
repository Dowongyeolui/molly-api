package org.example.mollyapi.payment.util;

import jakarta.annotation.PostConstruct;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.common.exception.error.impl.AESError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

@Component
public class AESUtil {

    @Value("${aes.secret-key}")
    private String AESKey;

    private static String AESKEY;

    @PostConstruct
    public void init() {
        AESKEY = AESKey;
    }

    public static String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKey secretKey = new SecretKeySpec(AESKEY.getBytes(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }

    public static String decrypt(String encryptedData){
        try {
            SecretKey secretKey = new SecretKeySpec(AESKEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedData)));
        } catch (Exception e){
            throw new CustomException(AESError.DECODE_FAIL);
        }
    }

    public static String decryptWithSalt(String encryptedData){
        try {
            // 1️⃣ Base64 디코딩
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);

            // 2️⃣ "Salted__" 문자열 확인 후 Salt 추출
            if (new String(Arrays.copyOfRange(encryptedBytes, 0, 8)).equals("Salted__")) {
                byte[] salt = Arrays.copyOfRange(encryptedBytes, 8, 16); // 8바이트 Salt 추출
                byte[] cipherText = Arrays.copyOfRange(encryptedBytes, 16, encryptedBytes.length); // 암호문

                // 3️⃣ Salt 기반 Key 생성
                SecretKey secretKey = generateKey(AESKEY, salt);

                // 4️⃣ AES/ECB 복호화 수행
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
            } else {
                throw new IllegalArgumentException("Invalid OpenSSL AES encrypted data format");
            }

        } catch (Exception e) {
            throw new RuntimeException("AES Decryption Failed", e);
        }
    }
    private static SecretKey generateKey(String password, byte[] salt) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] keyAndIv = new byte[32]; // AES-256 Key 크기 (32바이트)
        byte[] temp = new byte[0];

        // 반복하여 Key 확장
        for (int i = 0; i < 2; i++) {
            md5.update(temp);
            md5.update(password.getBytes(StandardCharsets.UTF_8));
            md5.update(salt);
            temp = md5.digest();
            System.arraycopy(temp, 0, keyAndIv, i * 16, 16);
        }

        return new SecretKeySpec(Arrays.copyOfRange(keyAndIv, 0, 32), "AES");
    }
}