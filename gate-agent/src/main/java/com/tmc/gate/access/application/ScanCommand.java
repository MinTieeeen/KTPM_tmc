package com.tmc.gate.access.application;

import java.time.Instant;
import java.util.Objects;

/**
 * Yêu cầu quét từ kiosk.
 *
 * @param receivedAt thời điểm controller nhận yêu cầu – dùng tính {@code elapsedMs}
 */
public record ScanCommand(String stationId, String raw, Instant receivedAt) {

    public ScanCommand {
        Objects.requireNonNull(stationId, "stationId");
        Objects.requireNonNull(raw, "raw");
        Objects.requireNonNull(receivedAt, "receivedAt");
    }

    /** Không để lộ chuỗi QR trong log (NFR-PRI-03). */
    @Override
    public String toString() {
        return "ScanCommand[stationId=" + stationId + ", receivedAt=" + receivedAt + "]";
    }
}
