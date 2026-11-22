package com.tmc.gate.access.application;

import com.tmc.gate.access.domain.Direction;
import com.tmc.gate.access.domain.RejectReason;
import com.tmc.gate.access.domain.ScanStatus;

import java.util.UUID;

/**
 * Kết quả xử lý một lượt quét.
 *
 * @param result       ACCEPTED | REJECTED | GATE_FAILED (không bao giờ PENDING)
 * @param reason       {@code null} khi ACCEPTED
 * @param employeeCode chỉ có với ACCEPTED, GATE_FAILED – không bao giờ có với REJECTED; không có họ tên (DP-12)
 * @param direction    chiều của trạm; {@code null} khi trạm không có trong bản sao
 */
public record ScanOutcome(
        ScanStatus result,
        RejectReason reason,
        String employeeCode,
        Direction direction,
        long elapsedMs,
        UUID eventId) {
}
