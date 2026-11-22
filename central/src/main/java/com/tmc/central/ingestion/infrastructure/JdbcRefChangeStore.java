package com.tmc.central.ingestion.infrastructure;

import com.tmc.central.ingestion.domain.RefChange;
import com.tmc.central.ingestion.domain.RefChangeKind;
import com.tmc.central.ingestion.domain.RefChangeStore;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/** Bảng {@code ref_changes} (PostgreSQL: {@code BIGSERIAL}, {@code JSONB}, {@code DISTINCT ON} – D1). */
@Repository
public class JdbcRefChangeStore implements RefChangeStore {

    private final JdbcClient jdbc;

    public JdbcRefChangeStore(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public long append(RefChangeKind kind, String key, String payloadJson, Instant changedAt) {
        return jdbc.sql("""
                        INSERT INTO ref_changes (kind, key, payload, changed_at)
                        VALUES (:kind, :key, CAST(:payload AS jsonb), :changedAt)
                        RETURNING version
                        """)
                .param("kind", kind.name())
                .param("key", key)
                .param("payload", payloadJson)
                .param("changedAt", Timestamp.from(changedAt))
                .query(Long.class)
                .single();
    }

    @Override
    public List<RefChange> latestSince(long since) {
        return jdbc.sql("""
                        SELECT DISTINCT ON (kind, key) version, kind, key, payload::text AS payload, changed_at
                        FROM ref_changes WHERE version > :since
                        ORDER BY kind, key, version DESC
                        """)
                .param("since", since)
                .query((rs, rowNum) -> new RefChange(
                        rs.getLong("version"),
                        RefChangeKind.valueOf(rs.getString("kind")),
                        rs.getString("key"),
                        rs.getString("payload"),
                        rs.getTimestamp("changed_at").toInstant()))
                .list();
    }

    @Override
    public long currentVersion() {
        return jdbc.sql("SELECT COALESCE(MAX(version), 0) FROM ref_changes")
                .query(Long.class)
                .single();
    }

    @Override
    public long countByKind(RefChangeKind kind) {
        return jdbc.sql("SELECT COUNT(*) FROM ref_changes WHERE kind = :kind")
                .param("kind", kind.name())
                .query(Long.class)
                .single();
    }
}
