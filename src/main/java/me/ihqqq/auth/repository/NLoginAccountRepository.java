package me.ihqqq.auth.repository;

import me.ihqqq.auth.entity.NLoginAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NLoginAccountRepository extends JpaRepository<NLoginAccount, Long> {

    Optional<NLoginAccount> findByLastNameIgnoreCase(String lastName);

    boolean existsByLastNameIgnoreCase(String lastName);

    Optional<NLoginAccount> findByUniqueId(String uniqueId);

    long countByCreationDateAfter(Instant instant);

    long countByMojangIdIsNotNull();

    List<NLoginAccount> findByLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String lastName, String email);
}