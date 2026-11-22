package com.tmc.central.iam.infrastructure;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.tmc.central.iam.application.CurrentUserService;
import com.tmc.central.iam.application.IamProperties;
import com.tmc.central.iam.domain.IssuedToken;
import com.tmc.central.iam.domain.Role;
import com.tmc.central.iam.domain.TokenIssuer;
import com.tmc.central.iam.domain.UserAccount;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Phát JWT RS256 (FR-03): {@code iss}, {@code sub=username}, {@code role}, {@code employeeCode} (chỉ EMPLOYEE),
 * {@code iat}, {@code exp = iat + ttl}, {@code jti}.
 */
@Component
public class JwtTokenIssuer implements TokenIssuer {

    static final String KEY_ID = "tmc-jwt";

    private final JwtEncoder encoder;
    private final String issuer;
    private final Duration ttl;

    public JwtTokenIssuer(JwtKeyProvider keys, IamProperties props) {
        RSAKey jwk = new RSAKey.Builder(keys.publicKey()).privateKey(keys.privateKey()).keyID(KEY_ID).build();
        this.encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
        this.issuer = props.jwt().issuer();
        this.ttl = props.jwt().ttl();
    }

    @Override
    public IssuedToken issue(UserAccount account, Instant now) {
        Instant expiresAt = now.plus(ttl);
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(account.username())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim(CurrentUserService.CLAIM_ROLE, account.role().name());
        if (account.role() == Role.EMPLOYEE && account.employeeCode() != null) {
            claims.claim(CurrentUserService.CLAIM_EMPLOYEE_CODE, account.employeeCode());
        }
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(KEY_ID).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
        return new IssuedToken(token, expiresAt);
    }
}
