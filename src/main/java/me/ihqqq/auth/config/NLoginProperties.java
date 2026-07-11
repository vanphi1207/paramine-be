package me.ihqqq.auth.config;

import me.ihqqq.auth.hash.HashAlgorithm;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "nlogin")
@Getter
@Setter
public class NLoginProperties {

    private HashAlgorithm algorithm = HashAlgorithm.BCRYPT2A;

    private int bcryptRounds = 10;

    private int pbkdf2Iterations = 10_000;
    private String pbkdf2Hmac = "HmacSHA512";

    private int argon2Iterations = 10;
    private int argon2MemoryKb = 65536;
    private int argon2Parallelism = 1;
}
