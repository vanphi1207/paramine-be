package me.ihqqq.auth.service;

import me.ihqqq.auth.dto.request.ChangePasswordRequest;
import me.ihqqq.auth.dto.request.LoginRequest;
import me.ihqqq.auth.dto.request.RegisterRequest;
import me.ihqqq.auth.dto.response.AccountResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private NLoginAccountRepository accountRepository;
    @Mock
    private AccountMetaRepository accountMetaRepository;
    @Mock
    private AccessTokenRepository accessTokenRepository;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticationService, "tokenValiditySeconds", 604800L);
    }

    private NLoginAccount sampleAccount(String hashedPassword) {
        return NLoginAccount.builder()
                .lastName("Steve")
                .uniqueId("uuid-123")
                .password(hashedPassword)
                .build();
    }

    private AccountMeta sampleMeta() {
        return AccountMeta.builder().uniqueId("uuid-123").role(Role.USER).banned(false).build();
    }

    @Test
    void login_success_returnsTokenAndAccount() {
        NLoginAccount account = sampleAccount("hashed-pw");
        when(accountRepository.findByLastNameIgnoreCase("Steve")).thenReturn(Optional.of(account));
        when(passwordHasher.verify("matkhau123", "hashed-pw")).thenReturn(true);
        when(accountMetaRepository.findById("uuid-123")).thenReturn(Optional.of(sampleMeta()));
        when(accountMapper.toAccountResponse(eq(account), any(AccountMeta.class)))
                .thenReturn(AccountResponse.builder().username("Steve").uniqueId("uuid-123").premium(false).role("USER").build());

        var response = authenticationService.login(new LoginRequest("Steve", "matkhau123"));

        assertThat(response.isAuthenticated()).isTrue();
        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getAccount().getUsername()).isEqualTo("Steve");
        verify(accessTokenRepository).save(any());
    }

    @Test
    void login_accountNotFound_throwsAppException() {
        when(accountRepository.findByLastNameIgnoreCase("KhongTonTai")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.login(new LoginRequest("KhongTonTai", "x")))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
    }

    @Test
    void login_accountNotRegistered_throwsAppException() {
        NLoginAccount account = sampleAccount(null);
        when(accountRepository.findByLastNameIgnoreCase("Steve")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> authenticationService.login(new LoginRequest("Steve", "matkhau123")))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ACCOUNT_NOT_REGISTERED);
    }

    @Test
    void login_wrongPassword_throwsAppException() {
        NLoginAccount account = sampleAccount("hashed-pw");
        when(accountRepository.findByLastNameIgnoreCase("Steve")).thenReturn(Optional.of(account));
        when(passwordHasher.verify("sai-mat-khau", "hashed-pw")).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.login(new LoginRequest("Steve", "sai-mat-khau")))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void login_bannedAccount_throwsAppException() {
        NLoginAccount account = sampleAccount("hashed-pw");
        when(accountRepository.findByLastNameIgnoreCase("Steve")).thenReturn(Optional.of(account));
        when(passwordHasher.verify("matkhau123", "hashed-pw")).thenReturn(true);
        when(accountMetaRepository.findById("uuid-123"))
                .thenReturn(Optional.of(AccountMeta.builder().uniqueId("uuid-123").role(Role.USER).banned(true).build()));

        assertThatThrownBy(() -> authenticationService.login(new LoginRequest("Steve", "matkhau123")))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ACCOUNT_BANNED);
    }

    @Test
    void register_success_savesHashedPasswordAndReturnsToken() {
        when(accountRepository.existsByLastNameIgnoreCase("Steve")).thenReturn(false);
        when(passwordHasher.hash("matkhau123")).thenReturn("hashed-pw");
        when(accountRepository.save(any(NLoginAccount.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accountMetaRepository.save(any(AccountMeta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accountMapper.toAccountResponse(any(), any()))
                .thenReturn(AccountResponse.builder().username("Steve").uniqueId("uuid-123").premium(false).role("USER").build());

        var response = authenticationService.register(
                new RegisterRequest("Steve", "matkhau123", "steve@example.com"));

        assertThat(response.isAuthenticated()).isTrue();
        verify(passwordHasher).hash("matkhau123");
        verify(accountRepository).save(argThat(acc -> "hashed-pw".equals(acc.getPassword())
                && "Steve".equals(acc.getLastName())));
        verify(accountMetaRepository).save(argThat(meta -> meta.getRole() == Role.USER && !meta.isBanned()));
    }

    @Test
    void register_usernameAlreadyExists_throwsAppException() {
        when(accountRepository.existsByLastNameIgnoreCase("Steve")).thenReturn(true);

        var request = new RegisterRequest("Steve", "matkhau123", "steve@example.com");
        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ACCOUNT_ALREADY_EXISTS);

        verify(accountRepository, never()).save(any());
    }

    @Test
    void changePassword_correctCurrentPassword_updatesHash() {
        NLoginAccount account = sampleAccount("hashed-old");
        when(accountRepository.findByLastNameIgnoreCase("Steve")).thenReturn(Optional.of(account));
        when(passwordHasher.verify("matkhauCu", "hashed-old")).thenReturn(true);
        when(passwordHasher.hash("matkhauMoi")).thenReturn("hashed-new");

        authenticationService.changePassword(new ChangePasswordRequest("Steve", "matkhauCu", "matkhauMoi"));

        verify(accountRepository).save(argThat(acc -> "hashed-new".equals(acc.getPassword())));
    }

    @Test
    void changePassword_wrongCurrentPassword_throwsAppExceptionAndDoesNotSave() {
        NLoginAccount account = sampleAccount("hashed-old");
        when(accountRepository.findByLastNameIgnoreCase("Steve")).thenReturn(Optional.of(account));
        when(passwordHasher.verify("sai", "hashed-old")).thenReturn(false);

        var request = new ChangePasswordRequest("Steve", "sai", "matkhauMoi");
        assertThatThrownBy(() -> authenticationService.changePassword(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CURRENT_PASSWORD_INCORRECT);

        verify(accountRepository, never()).save(any());
    }

    @Test
    void changePassword_accountNeverRegistered_throwsAppException() {
        NLoginAccount account = sampleAccount(null);
        when(accountRepository.findByLastNameIgnoreCase("Steve")).thenReturn(Optional.of(account));

        var request = new ChangePasswordRequest("Steve", "bat-ky-gi", "matkhauMoi");
        assertThatThrownBy(() -> authenticationService.changePassword(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CURRENT_PASSWORD_INCORRECT);
    }
}