package me.ihqqq.auth.repository;

import me.ihqqq.auth.entity.AccountMeta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountMetaRepository extends JpaRepository<AccountMeta, String> {
}