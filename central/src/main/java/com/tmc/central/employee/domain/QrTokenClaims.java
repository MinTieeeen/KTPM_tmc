package com.tmc.central.employee.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Nội dung cần ký vào mã QR; nonce và {@code kid} do bộ ký thêm vào. */
public record QrTokenClaims(UUID credentialId, Instant issuedAt, Instant expiresAt) {

    public QrTokenClaims {
        Objects.requireNonNull(credentialId, "credentialId");
        Objects.requireNonNull(issuedAt, "issuedAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
    }
}
