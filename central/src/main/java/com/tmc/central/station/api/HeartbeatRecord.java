package com.tmc.central.station.api;

import java.time.Instant;
import java.util.Map;

/**
 * Heartbeat đã nhận từ một gate.
 *
 * @param receivedAt      thời điểm Central nhận – căn cứ tính online/offline
 * @param lastSyncAt      có thể {@code null}
 * @param turnstileStatus stationId → {@code UP} | {@code DOWN} | {@code CIRCUIT_OPEN}
 */
public record HeartbeatRecord(String gateId, Instant sentAt, Instant receivedAt, long outboxDepth,
                              Instant lastSyncAt, Map<String, String> turnstileStatus) {

    public HeartbeatRecord {
        turnstileStatus = turnstileStatus == null ? Map.of() : Map.copyOf(turnstileStatus);
    }
}
