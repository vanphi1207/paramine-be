package me.ihqqq.auth.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.auth.dto.response.AdminAccountResponse;
import me.ihqqq.auth.dto.response.AdminOverviewResponse;
import me.ihqqq.auth.entity.AccountMeta;
import me.ihqqq.auth.entity.NLoginAccount;
import me.ihqqq.auth.entity.Role;
import me.ihqqq.auth.exception.AppException;
import me.ihqqq.auth.exception.ErrorCode;
import me.ihqqq.auth.repository.AccessTokenRepository;
import me.ihqqq.auth.repository.AccountMetaRepository;
import me.ihqqq.auth.repository.NLoginAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminService {

    NLoginAccountRepository accountRepository;
    AccountMetaRepository accountMetaRepository;
    AccessTokenRepository accessTokenRepository;

    @Transactional(readOnly = true)
    public AdminOverviewResponse getOverview() {
        Instant startOfToday = Instant.now().atZone(ZoneId.systemDefault())
                .truncatedTo(ChronoUnit.DAYS).toInstant();

        return AdminOverviewResponse.builder()
                .totalAccounts(accountRepository.count())
                .premiumAccounts(accountRepository.countByMojangIdIsNotNull())
                .newAccountsToday(accountRepository.countByCreationDateAfter(startOfToday))
                .onlineNow(accessTokenRepository.countByExpiryTimeAfter(Instant.now()))
                .build();
    }

    @Transactional(readOnly = true)
    public List<AdminAccountResponse> listAccounts(String search) {
        List<NLoginAccount> accounts = (search == null || search.isBlank())
                ? accountRepository.findAll()
                : accountRepository.findByLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search);

        return accounts.stream().map(this::toAdminAccountResponse).toList();
    }

    @Transactional
    public void setBanned(String uniqueId, boolean banned) {
        accountRepository.findByUniqueId(uniqueId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        AccountMeta meta = accountMetaRepository.findById(uniqueId)
                .orElseGet(() -> AccountMeta.builder().uniqueId(uniqueId).role(Role.USER).build());

        meta.setBanned(banned);
        meta.setUpdatedAt(Instant.now());
        accountMetaRepository.save(meta);
    }

    private AdminAccountResponse toAdminAccountResponse(NLoginAccount account) {
        AccountMeta meta = accountMetaRepository.findById(account.getUniqueId())
                .orElseGet(() -> AccountMeta.builder()
                        .uniqueId(account.getUniqueId())
                        .role(Role.USER)
                        .banned(false)
                        .build());

        return AdminAccountResponse.builder()
                .id(account.getUniqueId())
                .username(account.getLastName())
                .email(account.getEmail())
                .premium(account.isPremium())
                .banned(meta.isBanned())
                .role(meta.getRole().name())
                .createdAt(account.getCreationDate() == null ? null : account.getCreationDate().toString())
                .build();
    }
}