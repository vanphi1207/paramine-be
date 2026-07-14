package me.ihqqq.auth.repository;

import me.ihqqq.auth.entity.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface AccessTokenRepository extends JpaRepository<AccessToken, String> {

    long countByExpiryTimeAfter(Instant instant);
}