package com.tmc.contract.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Lượt quét đã chốt kết quả (payload của {@link EventType#SCAN_ATTEMPT_RECORDED}).
 *
 * @param credentialId có thể {@code null} khi token không đọc được (INV-07)
 * @param result       {@code ACCEPTED} | {@code REJECTED} | {@code GATE_FAILED}; lượt PENDING không được gửi
 * @param reason       tên hằng {@code RejectReason} của G1; {@code null} khi ACCEPTED
 */
public record ScanAttemptPayload(
        UUID eventId,
        String stationId,
        UUID credentialId,
        String result,
        String reason,
        Instant occurredAt) {

    public static final String RESULT_ACCEPTED = "ACCEPTED";
    public static final String RESULT_REJECTED = "REJECTED";
    public static final String RESULT_GATE_FAILED = "GATE_FAILED";
}
