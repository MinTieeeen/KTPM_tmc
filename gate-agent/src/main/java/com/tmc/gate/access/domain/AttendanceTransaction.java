package com.tmc.gate.access.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Giao dịch chấm công, chỉ tạo khi cổng đã xác nhận mở (FR-13, INV-05).
 *
 * @param scanEventId {@code eventId} của lượt quét tương ứng
 */
public record AttendanceTransaction(
        UUID eventId,
        UUID scanEventId,
        String employeeCode,
        String stationId,
        Direction direction,
        Instant occurredAt) {

    public AttendanceTransaction {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(scanEventId, "scanEventId");
        Objects.requireNonNull(employeeCode, "employeeCode");
        Objects.requireNonNull(stationId, "stationId");
        Objects.requireNonNull(direction, "direction");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}
