package com.tmc.central.iam.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/** Tham số C7 dưới {@code tmc.central.iam.*} (mục 6.6). */
@ConfigurationProperties("tmc.central.iam")
public record IamProperties(@DefaultValue Jwt jwt, @DefaultValue Password password) {

    /**
     * @param privateKeyPath file PEM PKCS#8 của khóa RSA; rỗng → profile demo tự sinh
     * @param publicKeyPath  file PEM X.509 của khóa công khai RSA
     */
    public record Jwt(
            @DefaultValue("tmc-central") String issuer,
            @DefaultValue("8h") Duration ttl,
            @DefaultValue("") String privateKeyPath,
            @DefaultValue("") String publicKeyPath) {
    }

    public record Password(@DefaultValue("10") int bcryptStrength) {
    }
}
