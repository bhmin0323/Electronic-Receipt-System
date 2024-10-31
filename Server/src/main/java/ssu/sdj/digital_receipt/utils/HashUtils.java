package ssu.sdj.digital_receipt.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

public class HashUtils {
    public static String generateBase64(String input) {
        // SecureRandom 인스턴스 생성
        SecureRandom random = new SecureRandom();

        // 6바이트 크기의 랜덤 바이트 배열 생성 (Base64로 8자리 문자열 생성)
        byte[] randomBytes = new byte[6];  // 6바이트 => 8자리 ~ 11자리 Base64 URL Safe
        random.nextBytes(randomBytes);

        // Base64 URL Safe 인코딩 (패딩 제거)
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        return encoded.substring(0, 10);
    }

    // Base64 디코딩 함수
    // Base64 디코딩 후 CP949로 디코딩 함수
    public static String base64DecodeToCP949(String data) {
        // Base64 디코딩
        byte[] decodedBytes = Base64.getDecoder().decode(data);

        // CP949로 디코딩
        return new String(decodedBytes, Charset.forName("CP949"));
    }

    // 8자리 랜덤 숫자 생성 함수
    public static String generateRandomNumber() {
        Random random = new Random();
        int number = 10000000 + random.nextInt(90000000);
        return Integer.toString(number);
    }

    public static byte[] digestHash(String input) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        return sha.digest(input.getBytes());
    }

    public static byte[] generateIV(){
        SecureRandom random = new SecureRandom();

        byte[] iv = new byte[16]; // AES 블록 크기는 16바이트
        random.nextBytes(iv); // 랜덤 IV 생성

        return iv;
    }

    public static String AES_Encrypt(String input, byte[] key, byte[] iv) throws Exception {

        // 3. AES-256을 위한 키 생성 (32바이트 사용)
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

        // 4. IV(초기화 벡터) 생성
        IvParameterSpec ivParams = new IvParameterSpec(iv);

        // 6. AES 암호화
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams);
        byte[] encryptedData = cipher.doFinal(input.getBytes());

        // 암호화된 데이터를 Base64로 출력
        String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedData);
        System.out.println("Encrypted Data: " + encryptedBase64);

        return encryptedBase64;
    }

    public static String AES_Decrypt(String input, String keyString, String ivString) throws Exception {
        byte[] key = digestHash(keyString);
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

        // 2. IV를 Base64에서 디코딩하여 바이트 배열로 변환
        byte[] iv = Base64.getDecoder().decode(ivString);
        IvParameterSpec ivParams = new IvParameterSpec(iv);

        // 3. 암호화된 데이터(Base64로 인코딩된 값)를 바이트 배열로 디코딩
        byte[] encryptedData = Base64.getDecoder().decode(input);

        // 4. AES 복호화 수행
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams);
        byte[] decryptedData = cipher.doFinal(encryptedData);

        // 복호화된 데이터를 문자열로 변환
        return new String(decryptedData);
    }
}
