package com.tmc.contract.heartbeat;

import java.time.Instant;
import java.util.Map;

/**
 * Nhịp tim của gate (SRS-SC-FR-07).
 *
 * @param lastSyncAt      có thể {@code null} khi gate chưa từng đồng bộ thành công
 * @param turnstileStatus stationId → {@code UP} | {@code DOWN} | {@code CIRCUIT_OPEN}
 */
public record Heartbeat(
        String gateId,
        Instant sentAt,
        long outboxDepth,
        Instant lastSyncAt,
        Map<String, String> turnstileStatus) {

    public static final String TURNSTILE_UP = "UP";
    public static final String TURNSTILE_DOWN = "DOWN";
    public static final String TURNSTILE_CIRCUIT_OPEN = "CIRCUIT_OPEN";

    public Heartbeat {
        turnstileStatus = turnstileStatus == null ? Map.of() : Map.copyOf(turnstileStatus);
    }
}
