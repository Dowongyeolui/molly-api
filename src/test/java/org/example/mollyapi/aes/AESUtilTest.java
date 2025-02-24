package org.example.mollyapi.aes;

import org.example.mollyapi.payment.util.AESUtil;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.common.exception.error.impl.AESError;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class AESUtilTest {

    private static final String AES_SECRET_KEY = "Rhcekfflsrkaskan5Rhcekfflsrkask1"; // ✅ 32바이트 키

    @BeforeAll
    static void setUp() {
        // ✅ Reflection을 이용하여 static 필드 AESKey 설정
        try {
            java.lang.reflect.Field field = AESUtil.class.getDeclaredField("AESKEY");
            field.setAccessible(true);
            field.set(null, AES_SECRET_KEY);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set AESKey for testing", e);
        }
    }

    @Test
    void testEncryptAndDecrypt_Success() throws Exception {
        // 원본 데이터
        String originalData = "010-5134-1111";

        // ✅ AES 암호화
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKey secretKey = new SecretKeySpec(AES_SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        String encryptedData = Base64.getEncoder().encodeToString(cipher.doFinal(originalData.getBytes(StandardCharsets.UTF_8)));

        // ✅ AESUtil을 이용한 복호화
        String decryptedData = AESUtil.decrypt(encryptedData);

        // ✅ 원본과 복호화된 값 비교
        assertEquals(originalData, decryptedData);

        // ✅ 결과 출력
        System.out.println("originalData: " + originalData);
        System.out.println("encryptedData: " + encryptedData);
        System.out.println("decryptedData: " + decryptedData);
    }

    @Test
    void testDecryptWithSalt_Success() {
        // ✅ OpenSSL AES 암호화된 데이터 (테스트용)
        String encryptedData = "U2FsdGVkX1/gSGVDRJeUW/pUpxm77bCPVgIouTLaFtw=";

        // ✅ 복호화 수행
        String decryptedData = AESUtil.decryptWithSalt(encryptedData);

        // ✅ 결과 출력
        System.out.println("Decrypted with Salt: " + decryptedData);
    }

    @Test
    void testDecrypt_InvalidData_ShouldThrowException() {
        // 잘못된 암호화 데이터
        String invalidData = "InvalidEncryptedString123==";

        // ✅ 예외 발생 테스트
        CustomException exception = assertThrows(CustomException.class, () -> AESUtil.decrypt(invalidData));

        // ✅ 예외 메시지 검증
        assertEquals(AESError.DECODE_FAIL.getMessage(), exception.getMessage());

        // ✅ 결과 출력
        System.out.println("AESError Message: " + AESError.DECODE_FAIL.getMessage());
        System.out.println("Exception: " + exception);
    }
}