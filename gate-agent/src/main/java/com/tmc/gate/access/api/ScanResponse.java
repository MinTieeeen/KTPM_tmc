package com.tmc.gate.access.api;

import com.tmc.gate.access.application.ScanOutcome;

import java.util.UUID;

/**
 * Phản hồi của {@code POST /scan}. Trường {@code null} bị bỏ khỏi JSON (cấu hình Jackson {@code non_null}):
 * {@code reason} vắng khi ACCEPTED, {@code employeeCode} không bao giờ có khi REJECTED.
 */
public record ScanResponse(
        String result,
        String reason,
        String employeeCode,
        String direction,
        long elapsedMs,
        UUID eventId) {

    static ScanResponse from(ScanOutcome o) {
        return new ScanResponse(
                o.result().name(),
                o.reason() == null ? null : o.reason().name(),
                o.employeeCode(),
                o.direction() == null ? null : o.direction().name(),
                o.elapsedMs(),
                o.eventId());
    }
}
