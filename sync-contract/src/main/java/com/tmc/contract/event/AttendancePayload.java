package com.tmc.contract.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Giao dịch chấm công (payload của {@link EventType#ATTENDANCE_RECORDED});
 * {@code scanEventId} tham chiếu lượt quét đã ACCEPTED.
 *
 * @param direction {@code IN} | {@code OUT}
 */
public record AttendancePayload(
        UUID eventId,
        UUID scanEventId,
        String employeeCode,
        String stationId,
        String direction,
        Instant occurredAt) {

    public static final String DIRECTION_IN = "IN";
    public static final String DIRECTION_OUT = "OUT";
}
