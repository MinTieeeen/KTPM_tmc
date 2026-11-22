package com.tmc.gate.access.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Token QR đã được kiểm định dạng, chữ ký và thời hạn. */
public record VerifiedToken(
        UUID credentialId,
        String nonce,
        String kid,
        Instant issuedAt,
        Instant expiresAt) {

    public VerifiedToken {
        Objects.requireNonNull(credentialId, "credentialId");
        Objects.requireNonNull(nonce, "nonce");
        Objects.requireNonNull(kid, "kid");
        Objects.requireNonNull(issuedAt, "issuedAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
    }
}
