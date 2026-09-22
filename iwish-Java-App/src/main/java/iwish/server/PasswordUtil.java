package iwish.server;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

// password hashing
public final class PasswordUtil {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ITERATIONS = 120_000;
    private static final int KEY_BITS = 256;

    private PasswordUtil() {
    }

    public static String newSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String password, String salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(),
                    Base64.getDecoder().decode(salt), ITERATIONS, KEY_BITS);
            byte[] key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(key);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing unavailable", e);
        }
    }

    public static boolean matches(String password, String salt, String expectedHash) {
        byte[] actual = hash(password, salt).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(actual, expectedHash.getBytes(StandardCharsets.UTF_8));
    }
}
