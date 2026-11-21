package com.tmc.gate.guard.api;

import com.tmc.gate.access.domain.ScanAttempt;

import java.time.Instant;
import java.util.UUID;

/** Một lượt PENDING cần đối soát; không có họ tên, không có chuỗi QR (FR-04, DP-12). */
public record PendingScanView(UUID eventId, String stationId, UUID credentialId, Instant occurredAt) {

    static PendingScanView from(ScanAttempt a) {
        return new PendingScanView(a.eventId(), a.stationId(), a.credentialId(), a.occurredAt());
    }
}
