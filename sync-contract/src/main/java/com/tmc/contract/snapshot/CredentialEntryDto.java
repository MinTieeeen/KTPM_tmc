package com.tmc.contract.snapshot;

import java.time.Instant;
import java.util.UUID;

/**
 * Một credential trong bản sao tại cổng; không chứa họ tên (SRS-SC-FR-06, DP-12).
 *
 * @param status {@code ACTIVE} | {@code REVOKED}
 */
public record CredentialEntryDto(
        UUID credentialId,
        String employeeCode,
        String status,
        boolean employeeActive,
        Instant updatedAt) {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_REVOKED = "REVOKED";
}
