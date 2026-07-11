package me.ihqqq.auth.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import me.ihqqq.auth.dto.request.*;
import me.ihqqq.auth.dto.response.AuthenticationResponse;
import me.ihqqq.auth.dto.response.IntrospectResponse;
import me.ihqqq.auth.entity.InvalidatedToken;
import me.ihqqq.auth.entity.NLoginAccount;
import me.ihqqq.auth.exception.AppException;
import me.ihqqq.auth.exception.ErrorCode;
import me.ihqqq.auth.hash.PasswordHasher;
import me.ihqqq.auth.mapper.AccountMapper;
import me.ihqqq.auth.repository.InvalidatedTokenRepository;
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

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService {

    NLoginAccountRepository accountRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    PasswordHasher passwordHasher;
    AccountMapper accountMapper;

    @NonFinal
    @Value("${jwt.signer-key}")
    protected String signerKey;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long validDuration;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long refreshableDuration;

    @Transactional(readOnly = true)
    public AuthenticationResponse login(LoginRequest request) throws JOSEException {
        NLoginAccount account = accountRepository.findByLastNameIgnoreCase(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (!account.isRegistered()) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_REGISTERED);
        }

        boolean matches = passwordHasher.verify(request.getPassword(), account.getPassword());
        if (!matches) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        var token = generateToken(account);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .account(accountMapper.toAccountResponse(account))
                .build();
    }

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) throws JOSEException {
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
        log.info("Account registered from web: {}", saved.getLastName());

        var token = generateToken(saved);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .account(accountMapper.toAccountResponse(saved))
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

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        boolean isValid = true;
        try {
            verifyToken(request.getToken(), false);
        } catch (AppException e) {
            isValid = false;
        }
        return IntrospectResponse.builder().valid(isValid).build();
    }

    @Transactional
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), true);
        invalidateToken(signedJWT);
    }

    @Transactional
    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), true);
        invalidateToken(signedJWT);

        String username = signedJWT.getJWTClaimsSet().getSubject();
        NLoginAccount account = accountRepository.findByLastNameIgnoreCase(username)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        var token = generateToken(account);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .account(accountMapper.toAccountResponse(account))
                .build();
    }

    private void invalidateToken(SignedJWT signedJWT) throws ParseException {
        String jit = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        invalidatedTokenRepository.save(InvalidatedToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build());
    }

    private String generateToken(NLoginAccount account) throws JOSEException {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(account.getLastName())
                .issuer("paramine.fun")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(validDuration, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("uniqueId", account.getUniqueId())
                .claim("premium", account.isPremium())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        jwsObject.sign(new MACSigner(signerKey.getBytes()));

        return jwsObject.serialize();
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(signerKey.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = isRefresh
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant()
                .plus(refreshableDuration, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean verified = signedJWT.verify(verifier);
        if (!(verified && expiryTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }
}