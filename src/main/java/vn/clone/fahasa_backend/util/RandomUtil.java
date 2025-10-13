package vn.clone.fahasa_backend.util;

import java.security.SecureRandom;

public class RandomUtil {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int KEY_LENGTH = 36;

    static {
        SECURE_RANDOM.nextBytes(new byte[64]);
    }

    public static String generateActivateKey() {
        StringBuilder keyBuilder = new StringBuilder();
        String charStr = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";
        char[] chars = charStr.toCharArray();
        int gap = chars.length;

        for (int i = 0; i < KEY_LENGTH; i++) {
            int randomInt = SECURE_RANDOM.nextInt(gap);
            keyBuilder.appendCodePoint(chars[randomInt]);
        }
        return keyBuilder.toString();
    }
}