package com.tmc.central.ingestion.infrastructure;

import com.tmc.central.ingestion.domain.IngestedAttendance;
import com.tmc.central.ingestion.domain.IngestedScanAttempt;
import com.tmc.central.ingestion.domain.IngestionStore;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

/**
 * Lưu sự kiện bằng {@code INSERT … ON CONFLICT DO NOTHING} (INV-06, D1); số dòng chèn được = 0 nghĩa là trùng.
 * Chỉ có INSERT, không UPDATE/DELETE (NFR-03, DP-02). Chạy trong transaction của use case.
 */
@Repository
public class JdbcIngestionStore implements IngestionStore {

    private final JdbcClient jdbc;

    public JdbcIngestionStore(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean insertScanIfAbsent(IngestedScanAttempt a) {
        return jdbc.sql("""
                        INSERT INTO scan_attempts
                            (event_id, gate_id, station_id, credential_id, result, reason, occurred_at, received_at)
                        VALUES (:eventId, :gateId, :stationId, :credentialId, :result, :reason, :occurredAt, :receivedAt)
                        ON CONFLICT (event_id) DO NOTHING
                        """)
                .param("eventId", a.eventId())
                .param("gateId", a.gateId())
                .param("stationId", a.stationId())
                .param("credentialId", a.credentialId())
                .param("result", a.result())
                .param("reason", a.reason())
                .param("occurredAt", Timestamp.from(a.occurredAt()))
                .param("receivedAt", Timestamp.from(a.receivedAt()))
                .update() == 1;
    }

    /** Không chỉ định cột xung đột: trùng {@code event_id} hoặc {@code scan_event_id} đều coi là trùng. */
    @Override
    public boolean insertAttendanceIfAbsent(IngestedAttendance a) {
        return jdbc.sql("""
                        INSERT INTO attendance_transactions
                            (event_id, scan_event_id, gate_id, employee_code, station_id, direction, occurred_at, received_at)
                        VALUES (:eventId, :scanEventId, :gateId, :employeeCode, :stationId, :direction, :occurredAt, :receivedAt)
                        ON CONFLICT DO NOTHING
                        """)
                .param("eventId", a.eventId())
                .param("scanEventId", a.scanEventId())
                .param("gateId", a.gateId())
                .param("employeeCode", a.employeeCode())
                .param("stationId", a.stationId())
                .param("direction", a.direction())
                .param("occurredAt", Timestamp.from(a.occurredAt()))
                .param("receivedAt", Timestamp.from(a.receivedAt()))
                .update() == 1;
    }
}
