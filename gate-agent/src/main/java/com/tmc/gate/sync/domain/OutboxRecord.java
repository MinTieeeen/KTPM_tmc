package com.tmc.gate.sync.domain;

import com.tmc.contract.event.EventType;

import java.time.Instant;
import java.util.UUID;

/**
 * Một bản ghi outbox chờ đẩy lên Central.
 *
 * @param payload JSON theo {@code sync-contract} ({@code ScanAttemptPayload} hoặc {@code AttendancePayload})
 */
public record OutboxRecord(long seq, UUID eventId, EventType type, String payload, Instant createdAt) {
}
