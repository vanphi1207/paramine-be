package me.ihqqq.auth.hash.impl;

import me.ihqqq.auth.hash.PasswordHasher;
import org.mindrot.jbcrypt.BCrypt;

public final class BCryptHasher implements PasswordHasher {

    private final int rounds;
    private final char version; // 'a' or 'y'

    public BCryptHasher(int rounds, char version) {
        this.rounds = rounds;
        this.version = version;
    }

    @Override
    public String hash(String plainPassword) {
        String rawSalt = BCrypt.gensalt(rounds, new java.security.SecureRandom());
        String salt = "$2" + version + rawSalt.substring(3);
        return BCrypt.hashpw(plainPassword, salt);
    }

    @Override
    public boolean verify(String plainPassword, String storedHash) {
        if (storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        try {
            // jBCrypt accepts both $2a$ and $2y$ prefixes transparently.
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}