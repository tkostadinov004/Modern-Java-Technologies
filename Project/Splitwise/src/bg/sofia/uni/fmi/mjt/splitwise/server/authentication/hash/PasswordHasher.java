package bg.sofia.uni.fmi.mjt.splitwise.server.authentication.hash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordHasher implements Hasher {
    private static final String ALGORITHM = "SHA-256";

    @Override
    public String hash(String content) {
        try {
            byte[] hashed = MessageDigest
                    .getInstance(ALGORITHM)
                    .digest(content.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
