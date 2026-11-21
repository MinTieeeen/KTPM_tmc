package com.tmc.gate.localstore.infrastructure;

import com.tmc.contract.event.EventType;
import com.tmc.gate.access.domain.AttendanceTransaction;
import com.tmc.gate.access.domain.Direction;
import com.tmc.gate.access.domain.NonceAlreadyUsedException;
import com.tmc.gate.access.domain.NonceRegistry;
import com.tmc.gate.access.domain.PresenceReader;
import com.tmc.gate.access.domain.RejectReason;
import com.tmc.gate.access.domain.ScanAttempt;
import com.tmc.gate.access.domain.ScanAttemptStore;
import com.tmc.gate.access.domain.ScanStatus;
import com.tmc.gate.access.domain.TransactionStore;
import com.tmc.gate.sync.domain.NonceCleaner;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Vòng đời lượt quét, giao dịch chấm công, presence và nonce. Mỗi thao tác ghi thêm bản ghi outbox
 * <b>trong cùng transaction</b> (transactional outbox, ADR-03). Chuyển trạng thái chỉ hợp lệ từ PENDING,
 * được bảo đảm bằng điều kiện SQL {@code WHERE status = 'PENDING'} (DP-02).
 */
@Repository
public class SqliteScanStore implements ScanAttemptStore, TransactionStore, NonceRegistry, PresenceReader, NonceCleaner {

    private static final RowMapper<ScanAttempt> ATTEMPT_MAPPER = (rs, rowNum) -> {
        String credentialId = rs.getString("credential_id");
        String reason = rs.getString("reason");
        return ScanAttempt.restore(
                UUID.fromString(rs.getString("event_id")),
                rs.getString("station_id"),
                credentialId == null ? null : UUID.fromString(credentialId),
                ScanStatus.valueOf(rs.getString("status")),
                reason == null ? null : RejectReason.valueOf(reason),
                SqliteTime.parse(rs.getString("occurred_at")),
                SqliteTime.parse(rs.getString("finalized_at")));
    };

    private final JdbcClient jdbc;
    private final SqliteOutboxStore outbox;
    private final OutboxPayloadMapper payloads;

    public SqliteScanStore(JdbcClient jdbc, SqliteOutboxStore outbox, OutboxPayloadMapper payloads) {
        this.jdbc = jdbc;
        this.outbox = outbox;
        this.payloads = payloads;
    }

    // ---------- ScanAttemptStore ----------

    @Override
    @Transactional
    public void recordPending(ScanAttempt attempt, String nonce, Instant nonceExpiresAt) {
        // Đánh dấu nonce trước: nếu trùng thì không ghi gì (INSERT OR IGNORE trả 0 dòng).
        int inserted = jdbc.sql("INSERT OR IGNORE INTO used_nonces (nonce, expires_at) VALUES (:nonce, :exp)")
                .param("nonce", nonce)
                .param("exp", SqliteTime.format(nonceExpiresAt))
                .update();
        if (inserted == 0) {
            throw new NonceAlreadyUsedException("Nonce đã được dùng cho lượt quét khác (eventId=" + attempt.eventId() + ")");
        }
        insertAttempt(attempt);
    }

    @Override
    @Transactional
    public void recordRejected(ScanAttempt attempt) {
        if (attempt.status() != ScanStatus.REJECTED) {
            throw new IllegalArgumentException("Lượt không ở trạng thái REJECTED: " + attempt);
        }
        insertAttempt(attempt);
        outbox.append(attempt.eventId(), EventType.SCAN_ATTEMPT_RECORDED,
                payloads.scanAttemptJson(attempt), attempt.finalizedAt());
    }

    @Override
    @Transactional
    public void markGateFailed(UUID eventId, RejectReason reason, Instant at) {
        int updated = jdbc.sql("""
                        UPDATE scan_attempts SET status = 'GATE_FAILED', reason = :reason, finalized_at = :at
                        WHERE event_id = :eventId AND status = 'PENDING'
                        """)
                .param("reason", reason.name())
                .param("at", SqliteTime.format(at))
                .param("eventId", eventId.toString())
                .update();
        requireTransition(updated, eventId);
        outbox.append(eventId, EventType.SCAN_ATTEMPT_RECORDED, payloads.scanAttemptJson(loadAttempt(eventId)), at);
    }

    @Override
    public List<ScanAttempt> findPending() {
        return jdbc.sql("SELECT * FROM scan_attempts WHERE status = 'PENDING' ORDER BY occurred_at")
                .query(ATTEMPT_MAPPER)
                .list();
    }

    // ---------- TransactionStore ----------

    @Override
    @Transactional
    public void confirmAccepted(UUID scanEventId, AttendanceTransaction tx) {
        int updated = jdbc.sql("""
                        UPDATE scan_attempts SET status = 'ACCEPTED', reason = NULL, finalized_at = :at
                        WHERE event_id = :eventId AND status = 'PENDING'
                        """)
                .param("at", SqliteTime.format(tx.occurredAt()))
                .param("eventId", scanEventId.toString())
                .update();
        requireTransition(updated, scanEventId);

        jdbc.sql("""
                        INSERT INTO attendance_transactions
                            (event_id, scan_event_id, employee_code, station_id, direction, occurred_at)
                        VALUES (:eventId, :scanEventId, :code, :station, :direction, :at)
                        """)
                .param("eventId", tx.eventId().toString())
                .param("scanEventId", scanEventId.toString())
                .param("code", tx.employeeCode())
                .param("station", tx.stationId())
                .param("direction", tx.direction().name())
                .param("at", SqliteTime.format(tx.occurredAt()))
                .update();

        jdbc.sql("""
                        INSERT INTO presence (employee_code, last_direction, last_at) VALUES (:code, :direction, :at)
                        ON CONFLICT(employee_code) DO UPDATE SET
                            last_direction = excluded.last_direction, last_at = excluded.last_at
                        """)
                .param("code", tx.employeeCode())
                .param("direction", tx.direction().name())
                .param("at", SqliteTime.format(tx.occurredAt()))
                .update();

        outbox.append(scanEventId, EventType.SCAN_ATTEMPT_RECORDED,
                payloads.scanAttemptJson(loadAttempt(scanEventId)), tx.occurredAt());
        outbox.append(tx.eventId(), EventType.ATTENDANCE_RECORDED, payloads.attendanceJson(tx), tx.occurredAt());
    }

    // ---------- NonceRegistry ----------

    @Override
    public boolean isUsed(String nonce) {
        return jdbc.sql("SELECT COUNT(*) FROM used_nonces WHERE nonce = :nonce")
                .param("nonce", nonce)
                .query(Integer.class)
                .single() > 0;
    }

    // ---------- PresenceReader ----------

    @Override
    public Optional<Direction> lastAcceptedDirection(String employeeCode) {
        return jdbc.sql("SELECT last_direction FROM presence WHERE employee_code = :code")
                .param("code", employeeCode)
                .query(String.class)
                .optional()
                .map(Direction::valueOf);
    }

    // ---------- NonceCleaner ----------

    @Override
    @Transactional
    public int purgeExpired(Instant now) {
        return jdbc.sql("DELETE FROM used_nonces WHERE expires_at < :now")
                .param("now", SqliteTime.format(now))
                .update();
    }

    // ---------- nội bộ ----------

    private void insertAttempt(ScanAttempt a) {
        jdbc.sql("""
                        INSERT INTO scan_attempts
                            (event_id, station_id, credential_id, status, reason, occurred_at, finalized_at)
                        VALUES (:eventId, :station, :credentialId, :status, :reason, :occurredAt, :finalizedAt)
                        """)
                .param("eventId", a.eventId().toString())
                .param("station", a.stationId())
                .param("credentialId", a.credentialId() == null ? null : a.credentialId().toString())
                .param("status", a.status().name())
                .param("reason", a.reason() == null ? null : a.reason().name())
                .param("occurredAt", SqliteTime.format(a.occurredAt()))
                .param("finalizedAt", SqliteTime.format(a.finalizedAt()))
                .update();
    }

    private ScanAttempt loadAttempt(UUID eventId) {
        return jdbc.sql("SELECT * FROM scan_attempts WHERE event_id = :eventId")
                .param("eventId", eventId.toString())
                .query(ATTEMPT_MAPPER)
                .single();
    }

    private static void requireTransition(int updatedRows, UUID eventId) {
        if (updatedRows == 0) {
            throw new IllegalStateException("Lượt quét " + eventId + " không tồn tại hoặc không còn PENDING");
        }
    }
}
