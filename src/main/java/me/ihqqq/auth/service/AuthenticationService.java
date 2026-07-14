package me.ihqqq.auth.service;

import me.ihqqq.auth.dto.request.*;
import me.ihqqq.auth.dto.response.AuthenticationResponse;
import me.ihqqq.auth.dto.response.IntrospectResponse;
import me.ihqqq.auth.entity.AccessToken;
import me.ihqqq.auth.entity.AccountMeta;
import me.ihqqq.auth.entity.NLoginAccount;
import me.ihqqq.auth.entity.Role;
import me.ihqqq.auth.exception.AppException;
import me.ihqqq.auth.exception.ErrorCode;
import me.ihqqq.auth.hash.PasswordHasher;
import me.ihqqq.auth.mapper.AccountMapper;
import me.ihqqq.auth.repository.AccessTokenRepository;
import me.ihqqq.auth.repository.AccountMetaRepository;
import me.ihqqq.auth.repository.NLoginAccountRepository;
import me.ihqqq.auth.util.OfflineUuid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService {

    NLoginAccountRepository accountRepository;
    AccountMetaRepository accountMetaRepository;
    AccessTokenRepository accessTokenRepository;
    PasswordHasher passwordHasher;
    AccountMapper accountMapper;

    @NonFinal
    @Value("${auth.token-validity-seconds}")
    protected long tokenValiditySeconds;

    @Transactional
    public AuthenticationResponse login(LoginRequest request) {
        NLoginAccount account = accountRepository.findByLastNameIgnoreCase(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (!account.isRegistered()) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_REGISTERED);
        }

        boolean matches = passwordHasher.verify(request.getPassword(), account.getPassword());
        if (!matches) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        AccountMeta meta = ensureMeta(account);
        if (meta.isBanned()) {
            throw new AppException(ErrorCode.ACCOUNT_BANNED);
        }

        String token = issueToken(account.getLastName());
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .account(accountMapper.toAccountResponse(account, meta))
                .build();
    }

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (accountRepository.existsByLastNameIgnoreCase(request.getUsername())) {
            throw new AppException(ErrorCode.ACCOUNT_ALREADY_EXISTS);
        }

        Instant now = Instant.now();
        NLoginAccount account = NLoginAccount.builder()
                .lastName(request.getUsername())
                .uniqueId(OfflineUuid.forUsername(request.getUsername()))
                .password(passwordHasher.hash(request.getPassword()))
                .creationDate(now)
                .lastSeen(now)
                .email(request.getEmail())
                .build();

        NLoginAccount saved = accountRepository.save(account);

        AccountMeta meta = accountMetaRepository.save(AccountMeta.builder()
                .uniqueId(saved.getUniqueId())
                .role(Role.USER)
                .banned(false)
                .updatedAt(now)
                .build());

        log.info("Account registered from web: {}", saved.getLastName());

        String token = issueToken(saved.getLastName());
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .account(accountMapper.toAccountResponse(saved, meta))
                .build();
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        NLoginAccount account = accountRepository.findByLastNameIgnoreCase(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (!account.isRegistered() || !passwordHasher.verify(request.getCurrentPassword(), account.getPassword())) {
            throw new AppException(ErrorCode.CURRENT_PASSWORD_INCORRECT);
        }

        account.setPassword(passwordHasher.hash(request.getNewPassword()));
        accountRepository.save(account);
        log.info("Password changed from web for account: {}", account.getLastName());
    }

    @Transactional(readOnly = true)
    public IntrospectResponse introspect(IntrospectRequest request) {
        boolean valid = request.getToken() != null && accessTokenRepository.findById(request.getToken())
                .filter(t -> t.getExpiryTime().isAfter(Instant.now()))
                .isPresent();
        return IntrospectResponse.builder().valid(valid).build();
    }

    @Transactional
    public void logout(LogoutRequest request) {
        if (request.getToken() != null) {
            accessTokenRepository.deleteById(request.getToken());
        }
    }

    @Transactional
    public AuthenticationResponse refreshToken(RefreshRequest request) {
        AccessToken oldToken = accessTokenRepository.findById(request.getToken())
                .filter(t -> t.getExpiryTime().isAfter(Instant.now()))
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        accessTokenRepository.deleteById(oldToken.getToken());

        NLoginAccount account = accountRepository.findByLastNameIgnoreCase(oldToken.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        AccountMeta meta = ensureMeta(account);
        if (meta.isBanned()) {
            throw new AppException(ErrorCode.ACCOUNT_BANNED);
        }

        String token = issueToken(account.getLastName());
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .account(accountMapper.toAccountResponse(account, meta))
                .build();
    }

    private AccountMeta ensureMeta(NLoginAccount account) {
        return accountMetaRepository.findById(account.getUniqueId())
                .orElseGet(() -> accountMetaRepository.save(AccountMeta.builder()
                        .uniqueId(account.getUniqueId())
                        .role(Role.USER)
                        .banned(false)
                        .updatedAt(Instant.now())
                        .build()));
    }

    private String issueToken(String username) {
        String token = UUID.randomUUID().toString();
        accessTokenRepository.save(AccessToken.builder()
                .token(token)
                .username(username)
                .expiryTime(Instant.now().plus(tokenValiditySeconds, ChronoUnit.SECONDS))
                .createdAt(Instant.now())
                .build());
        return token;
    }
}