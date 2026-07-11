package me.ihqqq.auth.hash.impl;

import me.ihqqq.auth.hash.PasswordHasher;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

public final class DigestHasher implements PasswordHasher {

    private static final int SALT_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final String jceAlgorithm;

    public DigestHasher(String jceAlgorithm) {
        this.jceAlgorithm = jceAlgorithm;
    }

    @Override
    public String hash(String plainPassword) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] digest = digest(salt, plainPassword);
        return HexFormat.of().formatHex(salt) + ":" + HexFormat.of().formatHex(digest);
    }

    @Override
    public boolean verify(String plainPassword, String storedHash) {
        if (storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        String[] parts = storedHash.split(":");
        if (parts.length != 2) {
            return false;
        }
        try {
            byte[] salt = HexFormat.of().parseHex(parts[0]);
            byte[] expected = HexFormat.of().parseHex(parts[1]);
            byte[] actual = digest(salt, plainPassword);
            return MessageDigest.isEqual(expected, actual);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private byte[] digest(byte[] salt, String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance(jceAlgorithm);
            md.update(salt);
            md.update(plainPassword.getBytes(StandardCharsets.UTF_8));
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unsupported digest algorithm: " + jceAlgorithm, e);
        }
    }
}
