package com.tmc.central.ingestion.domain;

import com.tmc.contract.event.AttendancePayload;
import com.tmc.contract.event.RejectedEvent;
import com.tmc.contract.event.ScanAttemptPayload;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Kiểm một sự kiện từ gate (FR-03, DP-11: không tin dữ liệu đến từ biên). Hàm thuần; trả lý do từ chối theo
 * {@link RejectedEvent} hoặc rỗng nếu hợp lệ.
 */
public final class EventValidator {

    private static final Set<String> SCAN_RESULTS = Set.of(
            ScanAttemptPayload.RESULT_ACCEPTED, ScanAttemptPayload.RESULT_REJECTED, ScanAttemptPayload.RESULT_GATE_FAILED);
    private static final Set<String> DIRECTIONS = Set.of(AttendancePayload.DIRECTION_IN, AttendancePayload.DIRECTION_OUT);
    private static final int MAX_REASON_LENGTH = 40;

    /**
     * @param stationFactory nhà máy của trạm trong payload (tra ở C3); rỗng khi trạm không tồn tại
     */
    public Optional<String> validateScan(GateContext gate, UUID envelopeEventId, ScanAttemptPayload p,
                                         Optional<String> stationFactory) {
        if (p == null || p.eventId() == null || !p.eventId().equals(envelopeEventId)
                || isBlank(p.stationId()) || p.occurredAt() == null || !SCAN_RESULTS.contains(p.result())
                || (p.reason() != null && p.reason().length() > MAX_REASON_LENGTH)) {
            return Optional.of(RejectedEvent.INVALID_PAYLOAD);
        }
        return checkStation(gate, stationFactory);
    }

    public Optional<String> validateAttendance(GateContext gate, UUID envelopeEventId, AttendancePayload p,
                                               Optional<String> stationFactory) {
        if (p == null || p.eventId() == null || !p.eventId().equals(envelopeEventId) || p.scanEventId() == null
                || isBlank(p.employeeCode()) || isBlank(p.stationId()) || p.occurredAt() == null
                || !DIRECTIONS.contains(p.direction())) {
            return Optional.of(RejectedEvent.INVALID_PAYLOAD);
        }
        return checkStation(gate, stationFactory);
    }

    private static Optional<String> checkStation(GateContext gate, Optional<String> stationFactory) {
        if (stationFactory.isEmpty()) {
            return Optional.of(RejectedEvent.UNKNOWN_STATION);
        }
        if (!Objects.equals(stationFactory.get(), gate.factoryId())) {
            return Optional.of(RejectedEvent.STATION_NOT_IN_FACTORY);
        }
        return Optional.empty();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
