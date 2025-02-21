package org.example.mollyapi.aes;

import org.example.mollyapi.payment.util.AESUtil;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.common.exception.error.impl.AESError;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class AESUtilTest {

    private static final String AES_SECRET_KEY = "Rhcekfflsrkaskan5Rhcekfflsrkask1"; // ✅ 32바이트 키

    @BeforeAll
    static void setUp() {
        // ✅ AESUtil의 AESKey를 강제 설정 (테스트 환경에서는 @Value 주입 불가)
        try {
            java.lang.reflect.Field field = AESUtil.class.getDeclaredField("AESKey");
            field.setAccessible(true);
            field.set(null, AES_SECRET_KEY);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set AESKey for testing", e);
        }
    }

    @Test
    void testDecrypt_Success() throws Exception {
        // 원본 데이터
        String originalData = "Hello, AES Encryption!";

        // AES 암호화
        Cipher cipher = Cipher.getInstance("AES");
        SecretKey secretKey = new SecretKeySpec(AES_SECRET_KEY.getBytes(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        String encryptedData = Base64.getEncoder().encodeToString(cipher.doFinal(originalData.getBytes()));

        // 복호화 수행
        String decryptedData = AESUtil.decrypt(encryptedData);

        // 결과 검증
        assertEquals(originalData, decryptedData);
        System.out.println("originalData: " + originalData + "\ndecryptedData: " + decryptedData);
    }

    @Test
    void testDecrypt_InvalidData_ShouldThrowException() {
        // 잘못된 암호화 데이터
        String invalidData = "InvalidEncryptedString123==";

        // 복호화 시 CustomException이 발생하는지 확인
        CustomException exception = assertThrows(CustomException.class, () -> AESUtil.decrypt(invalidData));

        // 예외 메시지 검증
        assertEquals(AESError.DECODE_FAIL.getMessage(), exception.getMessage());
        System.out.println("AESError Message: " + AESError.DECODE_FAIL.getMessage() + "\nexception: " + exception);
    }
}