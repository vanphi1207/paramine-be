package me.ihqqq.auth.hash.impl;

import me.ihqqq.auth.hash.PasswordHasher;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public final class Pbkdf2Hasher implements PasswordHasher {

    private static final int SALT_BYTES = 16;
    private static final int KEY_LENGTH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final int iterations;
    private final String hmacAlgorithm; // e.g. "HmacSHA512"

    public Pbkdf2Hasher(int iterations, String hmacAlgorithm) {
        this.iterations = iterations;
        this.hmacAlgorithm = hmacAlgorithm;
    }

    @Override
    public String hash(String plainPassword) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] derived = pbkdf2(plainPassword.toCharArray(), salt, iterations);
        return iterations + ":" + Base64.getEncoder().encodeToString(salt)
                + ":" + Base64.getEncoder().encodeToString(derived);
    }

    @Override
    public boolean verify(String plainPassword, String storedHash) {
        if (storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        String[] parts = storedHash.split(":");
        if (parts.length != 3) {
            return false;
        }
        try {
            int iters = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            byte[] actual = pbkdf2(plainPassword.toCharArray(), salt, iters);
            return constantTimeEquals(expected, actual);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        String jceName = toPbeAlgorithmName(hmacAlgorithm);
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(jceName);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Unsupported PBKDF2 algorithm: " + jceName, e);
        } finally {
            spec.clearPassword();
        }
    }

    private static String toPbeAlgorithmName(String hmacAlgorithm) {
        // "HmacSHA512" -> "PBKDF2WithHmacSHA512" (Java's naming convention)
        if (hmacAlgorithm.startsWith("PBKDF2")) {
            return hmacAlgorithm;
        }
        return "PBKDF2With" + hmacAlgorithm;
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
