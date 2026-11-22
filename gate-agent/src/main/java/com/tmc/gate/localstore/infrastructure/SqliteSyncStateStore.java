package com.tmc.gate.localstore.infrastructure;

import com.tmc.gate.access.domain.ConnectivityState;
import com.tmc.gate.sync.domain.SyncStateStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Bảng khóa–giá trị {@code sync_state}: mốc đồng bộ của G4 và ngưỡng mất kết nối (từ snapshot,
 * mặc định từ {@code tmc.gate.sync.default-offline-threshold}).
 */
@Repository
public class SqliteSyncStateStore implements SyncStateStore, ConnectivityState {

    static final String KEY_SNAPSHOT_VERSION = "snapshot_version";
    static final String KEY_LAST_SUCCESS_AT = "last_success_at";
    static final String KEY_OFFLINE_THRESHOLD_MIN = "offline_threshold_min";

    private final JdbcClient jdbc;
    private final int defaultOfflineThresholdMinutes;

    public SqliteSyncStateStore(JdbcClient jdbc,
                                @Value("${tmc.gate.sync.default-offline-threshold:8h}") Duration defaultOfflineThreshold) {
        this.jdbc = jdbc;
        this.defaultOfflineThresholdMinutes = Math.toIntExact(defaultOfflineThreshold.toMinutes());
    }

    @Override
    public long snapshotVersion() {
        return read(KEY_SNAPSHOT_VERSION).map(Long::parseLong).orElse(0L);
    }

    @Override
    @Transactional
    public void recordSuccessfulSync(Instant at) {
        write(KEY_LAST_SUCCESS_AT, SqliteTime.format(at));
    }

    @Override
    public Optional<Instant> lastSuccessfulSyncAt() {
        return read(KEY_LAST_SUCCESS_AT).map(SqliteTime::parse);
    }

    @Override
    public int offlineThresholdMinutes() {
        return read(KEY_OFFLINE_THRESHOLD_MIN).map(Integer::parseInt).orElse(defaultOfflineThresholdMinutes);
    }

    /** Ghi version và ngưỡng sau khi áp delta; chạy trong transaction của {@code applyDelta}. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void updateAfterDelta(long toVersion, Integer offlineThresholdMinutes) {
        write(KEY_SNAPSHOT_VERSION, Long.toString(toVersion));
        if (offlineThresholdMinutes != null) {
            write(KEY_OFFLINE_THRESHOLD_MIN, Integer.toString(offlineThresholdMinutes));
        }
    }

    private Optional<String> read(String key) {
        return jdbc.sql("SELECT value FROM sync_state WHERE key = :key")
                .param("key", key)
                .query(String.class)
                .optional();
    }

    private void write(String key, String value) {
        jdbc.sql("""
                        INSERT INTO sync_state (key, value) VALUES (:key, :value)
                        ON CONFLICT(key) DO UPDATE SET value = excluded.value
                        """)
                .param("key", key)
                .param("value", value)
                .update();
    }
}
