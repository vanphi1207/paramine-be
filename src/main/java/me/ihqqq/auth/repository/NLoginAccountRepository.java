package me.ihqqq.auth.repository;

import me.ihqqq.auth.entity.NLoginAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NLoginAccountRepository extends JpaRepository<NLoginAccount, Long> {

    Optional<NLoginAccount> findByLastNameIgnoreCase(String lastName);

    boolean existsByLastNameIgnoreCase(String lastName);
}