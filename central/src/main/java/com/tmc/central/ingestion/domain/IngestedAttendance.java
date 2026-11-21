package com.tmc.central.ingestion.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Giao dịch chấm công đã nhận từ gate (FR-04).
 *
 * @param direction {@code IN} | {@code OUT}
 */
public record IngestedAttendance(UUID eventId, UUID scanEventId, String gateId, String employeeCode,
                                 String stationId, String direction, Instant occurredAt, Instant receivedAt) {
}
