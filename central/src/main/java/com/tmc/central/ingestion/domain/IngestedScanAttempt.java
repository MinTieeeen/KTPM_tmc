package com.tmc.central.ingestion.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Lượt quét đã nhận từ gate (FR-05).
 *
 * @param credentialId có thể {@code null} (INV-07)
 * @param result       {@code ACCEPTED} | {@code REJECTED} | {@code GATE_FAILED}
 */
public record IngestedScanAttempt(UUID eventId, String gateId, String stationId, UUID credentialId, String result,
                                  String reason, Instant occurredAt, Instant receivedAt) {
}
