package com.tmc.gate.localstore.infrastructure;

import com.tmc.contract.snapshot.CredentialEntryDto;
import com.tmc.contract.snapshot.PublicKeyDto;
import com.tmc.contract.snapshot.SnapshotDelta;
import com.tmc.contract.snapshot.StationConfigDto;
import com.tmc.gate.access.domain.CredentialEntry;
import com.tmc.gate.access.domain.CredentialReader;
import com.tmc.gate.access.domain.CredentialStatus;
import com.tmc.gate.access.domain.Direction;
import com.tmc.gate.access.domain.PublicKeyReader;
import com.tmc.gate.sync.domain.CredentialWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bản sao credential và khóa công khai. G1 thấy hai interface chỉ đọc ({@link CredentialReader},
 * {@link PublicKeyReader}); G4 thấy {@link CredentialWriter} – hai phía có interface hẹp khác nhau (ISP).
 * Khóa công khai đã giải mã được cache theo {@code kid}; cache bị xóa mỗi lần áp delta.
 */
@Repository
public class SqliteCredentialStore implements CredentialReader, PublicKeyReader, CredentialWriter {

    private static final Logger log = LoggerFactory.getLogger(SqliteCredentialStore.class);

    private final JdbcClient jdbc;
    private final SqliteSyncStateStore syncState;
    private final Map<String, PublicKey> keyCache = new ConcurrentHashMap<>();

    public SqliteCredentialStore(JdbcClient jdbc, SqliteSyncStateStore syncState) {
        this.jdbc = jdbc;
        this.syncState = syncState;
    }

    // ---------- CredentialReader ----------

    @Override
    public Optional<CredentialEntry> findById(UUID credentialId) {
        return jdbc.sql("SELECT * FROM credentials WHERE credential_id = :id")
                .param("id", credentialId.toString())
                .query((rs, rowNum) -> new CredentialEntry(
                        UUID.fromString(rs.getString("credential_id")),
                        rs.getString("employee_code"),
                        CredentialStatus.valueOf(rs.getString("status")),
                        rs.getInt("employee_active") == 1,
                        SqliteTime.parse(rs.getString("updated_at"))))
                .optional();
    }

    // ---------- PublicKeyReader ----------

    @Override
    public Optional<PublicKey> findActive(String kid) {
        PublicKey cached = keyCache.get(kid);
        if (cached != null) {
            return Optional.of(cached);
        }
        Optional<PublicKey> loaded = loadActiveKey(kid);
        loaded.ifPresent(key -> keyCache.put(kid, key));
        return loaded;
    }

    private Optional<PublicKey> loadActiveKey(String kid) {
        return jdbc.sql("SELECT public_key FROM public_keys WHERE kid = :kid AND active = 1")
                .param("kid", kid)
                .query(String.class)
                .optional()
                .flatMap(encoded -> decodePublicKey(kid, encoded));
    }

    private static Optional<PublicKey> decodePublicKey(String kid, String base64) {
        try {
            byte[] der = Base64.getDecoder().decode(base64);
            return Optional.of(KeyFactory.getInstance("Ed25519").generatePublic(new X509EncodedKeySpec(der)));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            log.warn("Khóa công khai kid={} không giải mã được: {}", kid, e.getMessage());
            return Optional.empty();
        }
    }

    // ---------- CredentialWriter ----------

    @Override
    @Transactional
    public boolean applyDelta(SnapshotDelta delta) {
        long current = syncState.snapshotVersion();
        if (delta.fromVersion() != 0 && delta.fromVersion() != current) {
            log.warn("Bỏ qua delta lệch version: fromVersion={}, version hiện tại={}", delta.fromVersion(), current);
            return false;
        }

        delta.credentials().forEach(this::upsertCredential);
        delta.publicKeys().forEach(this::upsertPublicKey);
        delta.stations().forEach(this::upsertStation);
        Integer threshold = delta.settings() == null ? null : delta.settings().offlineThresholdMinutes();
        syncState.updateAfterDelta(delta.toVersion(), threshold);

        // Pool chỉ có một kết nối: lần đọc khóa kế tiếp phải chờ transaction này commit rồi mới nạp lại.
        keyCache.clear();
        log.info("Đã áp delta {} → {}: {} credential, {} khóa, {} trạm", delta.fromVersion(), delta.toVersion(),
                delta.credentials().size(), delta.publicKeys().size(), delta.stations().size());
        return true;
    }

    private void upsertCredential(CredentialEntryDto dto) {
        CredentialStatus status = CredentialStatus.valueOf(dto.status()); // giá trị lạ → rollback cả delta
        jdbc.sql("""
                        INSERT INTO credentials (credential_id, employee_code, status, employee_active, updated_at)
                        VALUES (:id, :code, :status, :active, :updatedAt)
                        ON CONFLICT(credential_id) DO UPDATE SET
                            employee_code = excluded.employee_code,
                            status = excluded.status,
                            employee_active = excluded.employee_active,
                            updated_at = excluded.updated_at
                        WHERE excluded.updated_at >= credentials.updated_at
                        """)
                .param("id", dto.credentialId().toString())
                .param("code", dto.employeeCode())
                .param("status", status.name())
                .param("active", dto.employeeActive() ? 1 : 0)
                .param("updatedAt", SqliteTime.format(dto.updatedAt()))
                .update();
    }

    private void upsertPublicKey(PublicKeyDto dto) {
        jdbc.sql("""
                        INSERT INTO public_keys (kid, public_key, active) VALUES (:kid, :key, :active)
                        ON CONFLICT(kid) DO UPDATE SET public_key = excluded.public_key, active = excluded.active
                        """)
                .param("kid", dto.kid())
                .param("key", dto.publicKeyBase64())
                .param("active", dto.active() ? 1 : 0)
                .update();
    }

    private void upsertStation(StationConfigDto dto) {
        Direction direction = Direction.valueOf(dto.direction());
        jdbc.sql("""
                        INSERT INTO stations (station_id, factory_id, direction, enabled, turnstile_id)
                        VALUES (:id, :factory, :direction, :enabled, :turnstile)
                        ON CONFLICT(station_id) DO UPDATE SET
                            factory_id = excluded.factory_id,
                            direction = excluded.direction,
                            enabled = excluded.enabled,
                            turnstile_id = excluded.turnstile_id
                        """)
                .param("id", dto.stationId())
                .param("factory", dto.factoryId())
                .param("direction", direction.name())
                .param("enabled", dto.enabled() ? 1 : 0)
                .param("turnstile", dto.turnstileId())
                .update();
    }
}
