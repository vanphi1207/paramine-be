package me.ihqqq.auth.hash;

public interface PasswordHasher {

    String hash(String plainPassword);

    boolean verify(String plainPassword, String storedHash);
}
