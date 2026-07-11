package me.ihqqq.auth.config;

import me.ihqqq.auth.hash.HasherFactory;
import me.ihqqq.auth.hash.PasswordHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class HasherConfig {

    private final NLoginProperties nLoginProperties;

    @Bean
    public PasswordHasher passwordHasher() {
        return HasherFactory.create(nLoginProperties);
    }
}
