package com.tmc.gate.localstore.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmc.contract.event.AttendancePayload;
import com.tmc.contract.event.ScanAttemptPayload;
import com.tmc.gate.access.domain.AttendanceTransaction;
import com.tmc.gate.access.domain.ScanAttempt;
import com.tmc.gate.access.domain.ScanStatus;
import org.springframework.stereotype.Component;

/**
 * Chuyển kiểu miền của G1 sang JSON payload của {@code sync-contract}. Nhờ lớp này {@code access.domain}
 * không phụ thuộc hợp đồng (D3).
 */
@Component
public class OutboxPayloadMapper {

    private final ObjectMapper objectMapper;

    public OutboxPayloadMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** Chỉ dùng cho lượt đã chốt kết quả – lượt PENDING không được gửi lên Central. */
    public String scanAttemptJson(ScanAttempt attempt) {
        if (attempt.status() == ScanStatus.PENDING) {
            throw new IllegalArgumentException("Không gửi lượt PENDING lên Central: " + attempt.eventId());
        }
        ScanAttemptPayload payload = new ScanAttemptPayload(
                attempt.eventId(),
                attempt.stationId(),
                attempt.credentialId(),
                attempt.status().name(),
                attempt.reason() == null ? null : attempt.reason().name(),
                attempt.occurredAt());
        return write(payload);
    }

    public String attendanceJson(AttendanceTransaction tx) {
        AttendancePayload payload = new AttendancePayload(
                tx.eventId(),
                tx.scanEventId(),
                tx.employeeCode(),
                tx.stationId(),
                tx.direction().name(),
                tx.occurredAt());
        return write(payload);
    }

    private String write(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new LocalStoreException("Không tuần tự hóa được payload outbox", e);
        }
    }
}
