package com.tmc.central.iam.infrastructure;

import com.tmc.central.iam.application.CurrentUserService;
import com.tmc.central.iam.domain.UserAccount;
import com.tmc.central.iam.domain.UserAccountRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Optional;

/**
 * {@link Jwt} → {@link JwtAuthenticationToken} với authority {@code ROLE_<role>}. Mỗi request kiểm tài khoản vẫn
 * còn và đang {@code enabled}, vai trò không đổi; nếu không → 401 (FR-06, NFR-SEC-04).
 * <p>Không khai báo là bean ({@code Converter} bean sẽ bị Spring MVC đăng ký vào conversion service chung);
 * được tạo trong {@link SecurityConfig}.
 */
public class JwtRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserAccountRepository accounts;

    public JwtRoleConverter(UserAccountRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String role = jwt.getClaimAsString(CurrentUserService.CLAIM_ROLE);
        Optional<UserAccount> account = accounts.findByUsername(jwt.getSubject());
        if (account.isEmpty() || !account.get().canLogin() || !account.get().role().name().equals(role)) {
            throw new DisabledException("Tài khoản không còn hiệu lực");
        }
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_" + role)), jwt.getSubject());
    }
}
