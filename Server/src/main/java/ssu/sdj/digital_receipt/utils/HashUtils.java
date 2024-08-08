package ssu.sdj.digital_receipt.utils;

import org.apache.commons.codec.digest.DigestUtils;

public class HashUtils {
    public static String generateHash(String input) {
        // SHA-256 해시 생성
        String hash = DigestUtils.sha256Hex(input);

        // 해시 값을 10자로 잘라서 반환
        return hash.substring(0, 10);
    }
}
