package com.tmc.central.iam.application;

import com.tmc.central.iam.api.LoginResponse;
import com.tmc.central.iam.domain.AuthenticationFailedException;
import com.tmc.central.iam.domain.IssuedToken;
import com.tmc.central.iam.domain.PasswordVerifier;
import com.tmc.central.iam.domain.TokenIssuer;
import com.tmc.central.iam.domain.UserAccount;
import com.tmc.central.iam.domain.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Locale;
import java.util.Optional;

/**
 * Đăng nhập (FR-25): tra tài khoản → so mật khẩu → kiểm {@code canLogin()} → phát token. Mọi nhánh thất bại ném
 * cùng {@link AuthenticationFailedException} (NFR-SEC-04).
 */
@Service
public class LoginUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginUseCase.class);

    private final UserAccountRepository accounts;
    private final PasswordVerifier passwords;
    private final TokenIssuer tokens;
    private final Clock clock;

    public LoginUseCase(UserAccountRepository accounts, PasswordVerifier passwords, TokenIssuer tokens, Clock clock) {
        this.accounts = accounts;
        this.passwords = passwords;
        this.tokens = tokens;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(String username, String password) {
        String normalized = username.trim().toLowerCase(Locale.ROOT);
        Optional<UserAccount> found = accounts.findByUsername(normalized);
        if (found.isEmpty()) {
            passwords.dummyCheck(password);
            throw fail(normalized);
        }
        UserAccount account = found.get();
        if (!passwords.matches(password, account.passwordHash()) || !account.canLogin()) {
            throw fail(normalized);
        }

        IssuedToken token = tokens.issue(account, clock.instant());
        log.info("Đăng nhập thành công: {} ({})", normalized, account.role());
        return new LoginResponse(token.accessToken(), token.expiresAt(), account.role().name(), account.employeeCode());
    }

    private static AuthenticationFailedException fail(String username) {
        log.info("Đăng nhập thất bại: {}", username);
        return new AuthenticationFailedException();
    }
}
