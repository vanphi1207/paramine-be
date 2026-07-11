package me.ihqqq.auth.hash.impl;

import me.ihqqq.auth.hash.PasswordHasher;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public final class Argon2Hasher implements PasswordHasher {

    private final Argon2 argon2;
    private final int iterations;
    private final int memoryKb;
    private final int parallelism;

    public Argon2Hasher(Argon2Factory.Argon2Types type, int iterations, int memoryKb, int parallelism) {
        this.argon2 = Argon2Factory.create(type);
        this.iterations = iterations;
        this.memoryKb = memoryKb;
        this.parallelism = parallelism;
    }

    @Override
    public String hash(String plainPassword) {
        char[] chars = plainPassword.toCharArray();
        try {
            return argon2.hash(iterations, memoryKb, parallelism, chars);
        } finally {
            argon2.wipeArray(chars);
        }
    }

    @Override
    public boolean verify(String plainPassword, String storedHash) {
        if (storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        char[] chars = plainPassword.toCharArray();
        try {
            return argon2.verify(storedHash, chars);
        } catch (IllegalArgumentException e) {
            return false;
        } finally {
            argon2.wipeArray(chars);
        }
    }
}
