package goskill.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Hash password dengan SHA-256 + salt acak. Format simpan: salt:hash (Base64). */
public class PasswordUtil {
    public static String hash(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + digest(salt, password);
    }

    public static boolean verify(String password, String stored) {
        String[] parts = stored.split(":");
        if (parts.length != 2) return false;
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        return digest(salt, password).equals(parts[1]);
    }

    private static String digest(byte[] salt, String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return Base64.getEncoder().encodeToString(md.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
