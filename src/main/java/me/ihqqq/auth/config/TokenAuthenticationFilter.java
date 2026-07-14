package me.ihqqq.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.ihqqq.auth.entity.AccountMeta;
import me.ihqqq.auth.entity.Role;
import me.ihqqq.auth.repository.AccessTokenRepository;
import me.ihqqq.auth.repository.AccountMetaRepository;
import me.ihqqq.auth.repository.NLoginAccountRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AccessTokenRepository accessTokenRepository;
    private final NLoginAccountRepository accountRepository;
    private final AccountMetaRepository accountMetaRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());

            accessTokenRepository.findById(token)
                    .filter(accessToken -> accessToken.getExpiryTime().isAfter(Instant.now()))
                    .flatMap(accessToken -> accountRepository.findByLastNameIgnoreCase(accessToken.getUsername()))
                    .ifPresent(account -> {
                        Role role = accountMetaRepository.findById(account.getUniqueId())
                                .map(AccountMeta::getRole)
                                .orElse(Role.USER);

                        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
                        var authentication = new UsernamePasswordAuthenticationToken(
                                account.getLastName(), null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
        }

        filterChain.doFilter(request, response);
    }
}