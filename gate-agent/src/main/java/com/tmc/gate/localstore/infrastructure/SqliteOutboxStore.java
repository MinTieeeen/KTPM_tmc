package com.tmc.gate.localstore.infrastructure;

import com.tmc.contract.event.EventType;
import com.tmc.gate.access.domain.OutboxMonitor;
import com.tmc.gate.sync.domain.OutboxRecord;
import com.tmc.gate.sync.domain.OutboxStore;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Bảng {@code outbox}: G1 thêm bản ghi (gián tiếp qua {@link SqliteScanStore}, cùng transaction),
 * G4 đọc theo lô và đánh dấu trạng thái (ADR-03).
 */
@Repository
public class SqliteOutboxStore implements OutboxStore, OutboxMonitor {

    private final JdbcClient jdbc;

    public SqliteOutboxStore(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Thêm một bản ghi PENDING; bắt buộc chạy trong transaction của thao tác nghiệp vụ (transactional outbox). */
    @Transactional(propagation = Propagation.MANDATORY)
    public void append(UUID eventId, EventType type, String payload, Instant createdAt) {
        jdbc.sql("""
                        INSERT INTO outbox (event_id, type, payload, status, created_at)
                        VALUES (:eventId, :type, :payload, 'PENDING', :createdAt)
                        """)
                .param("eventId", eventId.toString())
                .param("type", type.name())
                .param("payload", payload)
                .param("createdAt", SqliteTime.format(createdAt))
                .update();
    }

    @Override
    public List<OutboxRecord> fetchPending(int limit) {
        return jdbc.sql("""
                        SELECT seq, event_id, type, payload, created_at FROM outbox
                        WHERE status = 'PENDING' ORDER BY seq LIMIT :limit
                        """)
                .param("limit", limit)
                .query((rs, rowNum) -> new OutboxRecord(
                        rs.getLong("seq"),
                        UUID.fromString(rs.getString("event_id")),
                        EventType.valueOf(rs.getString("type")),
                        rs.getString("payload"),
                        SqliteTime.parse(rs.getString("created_at"))))
                .list();
    }

    @Override
    @Transactional
    public void markSynced(Collection<UUID> eventIds, Instant at) {
        if (eventIds.isEmpty()) {
            return;
        }
        jdbc.sql("""
                        UPDATE outbox SET status = 'SYNCED', synced_at = :at
                        WHERE event_id IN (:ids) AND status = 'PENDING'
                        """)
                .param("at", SqliteTime.format(at))
                .param("ids", eventIds.stream().map(UUID::toString).toList())
                .update();
    }

    @Override
    @Transactional
    public void markRejected(Map<UUID, String> reasons, Instant at) {
        reasons.forEach((eventId, reason) -> jdbc.sql("""
                        UPDATE outbox SET status = 'REJECTED', reject_reason = :reason, synced_at = :at
                        WHERE event_id = :eventId AND status = 'PENDING'
                        """)
                .param("reason", reason)
                .param("at", SqliteTime.format(at))
                .param("eventId", eventId.toString())
                .update());
    }

    @Override
    public long countPending() {
        return jdbc.sql("SELECT COUNT(*) FROM outbox WHERE status = 'PENDING'")
                .query(Long.class)
                .single();
    }

    @Override
    public long pendingCount() {
        return countPending();
    }
}
