package com.tmc.gate.access.domain;

import java.time.Instant;
import java.util.UUID;

/** Một credential trong bản sao cục bộ; không có họ tên (DP-12). */
public record CredentialEntry(
        UUID credentialId,
        String employeeCode,
        CredentialStatus status,
        boolean employeeActive,
        Instant updatedAt) {
}
