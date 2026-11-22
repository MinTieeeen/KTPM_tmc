package com.tmc.central.employee.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Tham số mã QR dưới {@code tmc.central.qr.*} (mục 6.7). {@code tokenTtl} không được vượt 60 s (NFR-SEC-05) –
 * kiểm khi khởi động.
 *
 * @param signingKey PKCS#8 base64 của khóa bí mật Ed25519 – không commit; rỗng → profile demo tự sinh
 * @param publicKey  X.509 base64 của khóa công khai tương ứng
 */
@ConfigurationProperties("tmc.central.qr")
public record QrProperties(
        @DefaultValue("60s") Duration tokenTtl,
        @DefaultValue("30s") Duration refreshAfter,
        @DefaultValue("k-demo") String keyId,
        @DefaultValue("") String signingKey,
        @DefaultValue("") String publicKey) {

    private static final Duration MAX_TTL = Duration.ofSeconds(60);

    public QrProperties {
        if (tokenTtl.isNegative() || tokenTtl.isZero() || tokenTtl.compareTo(MAX_TTL) > 0) {
            throw new IllegalStateException("tmc.central.qr.token-ttl phải trong (0, 60s], hiện là " + tokenTtl);
        }
        if (refreshAfter.isNegative() || refreshAfter.isZero() || refreshAfter.compareTo(tokenTtl) >= 0) {
            throw new IllegalStateException("tmc.central.qr.refresh-after phải dương và nhỏ hơn token-ttl");
        }
    }

    /** Không để lộ khóa bí mật trong log. */
    @Override
    public String toString() {
        return "QrProperties[tokenTtl=" + tokenTtl + ", refreshAfter=" + refreshAfter + ", keyId=" + keyId + "]";
    }
}
