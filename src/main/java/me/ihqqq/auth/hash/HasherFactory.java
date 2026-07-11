package me.ihqqq.auth.hash;

import me.ihqqq.auth.config.NLoginProperties;
import me.ihqqq.auth.hash.impl.Argon2Hasher;
import me.ihqqq.auth.hash.impl.BCryptHasher;
import me.ihqqq.auth.hash.impl.DigestHasher;
import me.ihqqq.auth.hash.impl.Pbkdf2Hasher;
import de.mkammerer.argon2.Argon2Factory;

public final class HasherFactory {

    private HasherFactory() {
    }

    public static PasswordHasher create(NLoginProperties props) {
        HashAlgorithm algorithm = props.getAlgorithm();
        switch (algorithm) {
            case MD5:
                return new DigestHasher("MD5");
            case SHA256:
                return new DigestHasher("SHA-256");
            case SHA512:
                return new DigestHasher("SHA-512");
            case BCRYPT2A:
                return new BCryptHasher(props.getBcryptRounds(), 'a');
            case BCRYPT2Y:
                return new BCryptHasher(props.getBcryptRounds(), 'y');
            case PBKDF2:
                return new Pbkdf2Hasher(props.getPbkdf2Iterations(), props.getPbkdf2Hmac());
            case ARGON2ID:
                return new Argon2Hasher(Argon2Factory.Argon2Types.ARGON2id,
                        props.getArgon2Iterations(), props.getArgon2MemoryKb(), props.getArgon2Parallelism());
            case ARGON2I:
                return new Argon2Hasher(Argon2Factory.Argon2Types.ARGON2i,
                        props.getArgon2Iterations(), props.getArgon2MemoryKb(), props.getArgon2Parallelism());
            case ARGON2D:
                return new Argon2Hasher(Argon2Factory.Argon2Types.ARGON2d,
                        props.getArgon2Iterations(), props.getArgon2MemoryKb(), props.getArgon2Parallelism());
            default:
                throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }
    }
}
