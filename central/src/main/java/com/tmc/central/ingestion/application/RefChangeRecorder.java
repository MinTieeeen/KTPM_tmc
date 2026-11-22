package com.tmc.central.ingestion.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmc.central.employee.api.CredentialState;
import com.tmc.central.employee.api.PublicKeyInfo;
import com.tmc.central.employee.api.QrCredentialChanged;
import com.tmc.central.employee.api.SigningKeyPublished;
import com.tmc.central.ingestion.domain.RefChangeKind;
import com.tmc.central.ingestion.domain.RefChangeStore;
import com.tmc.central.station.api.StationConfigChanged;
import com.tmc.central.station.api.StationInfo;
import com.tmc.contract.snapshot.CredentialEntryDto;
import com.tmc.contract.snapshot.PublicKeyDto;
import com.tmc.contract.snapshot.StationConfigDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Nghe sự kiện thay đổi dữ liệu tham chiếu của C2/C3 và ghi vào {@code ref_changes} (FR-06, DP-04). Payload là DTO
 * của {@code sync-contract} để {@code /sync/snapshot} chỉ cần đọc lại. {@code @ApplicationModuleListener}: chạy sau
 * commit, transaction riêng, được Spring Modulith lưu trong {@code event_publication} và giao lại nếu lỗi/khởi động lại.
 * Cũng được {@link RefDataBootstrapper} dùng để ghi dữ liệu ban đầu.
 */
@Component
public class RefChangeRecorder {

    private static final Logger log = LoggerFactory.getLogger(RefChangeRecorder.class);

    private final RefChangeStore store;
    private final ObjectMapper objectMapper;

    public RefChangeRecorder(RefChangeStore store, ObjectMapper objectMapper) {
        this.store = store;
        this.objectMapper = objectMapper;
    }

    @ApplicationModuleListener
    public void on(QrCredentialChanged e) {
        recordCredential(new CredentialEntryDto(e.credentialId(), e.employeeCode(), e.status(), e.employeeActive(),
                e.changedAt()), e.changedAt());
    }

    @ApplicationModuleListener
    public void on(SigningKeyPublished e) {
        recordPublicKey(new PublicKeyDto(e.kid(), e.publicKeyBase64(), e.active()), e.publishedAt());
    }

    @ApplicationModuleListener
    public void on(StationConfigChanged e) {
        recordStation(new StationConfigDto(e.stationId(), e.factoryId(), e.direction(), e.enabled(), e.turnstileId()),
                e.changedAt());
    }

    void recordCredential(CredentialState s, Instant changedAt) {
        recordCredential(new CredentialEntryDto(s.credentialId(), s.employeeCode(), s.status(), s.employeeActive(),
                s.updatedAt()), changedAt);
    }

    void recordPublicKey(PublicKeyInfo k, Instant changedAt) {
        recordPublicKey(new PublicKeyDto(k.kid(), k.publicKeyBase64(), k.active()), changedAt);
    }

    void recordStation(StationInfo s, Instant changedAt) {
        recordStation(new StationConfigDto(s.stationId(), s.factoryId(), s.direction(), s.enabled(), s.turnstileId()),
                changedAt);
    }

    private void recordCredential(CredentialEntryDto dto, Instant changedAt) {
        long v = store.append(RefChangeKind.CREDENTIAL, dto.credentialId().toString(), json(dto), changedAt);
        log.info("ref_changes v{}: credential {} của {} → {}", v, dto.credentialId(), dto.employeeCode(), dto.status());
    }

    private void recordPublicKey(PublicKeyDto dto, Instant changedAt) {
        long v = store.append(RefChangeKind.PUBLIC_KEY, dto.kid(), json(dto), changedAt);
        log.info("ref_changes v{}: khóa {} active={}", v, dto.kid(), dto.active());
    }

    private void recordStation(StationConfigDto dto, Instant changedAt) {
        long v = store.append(RefChangeKind.STATION, dto.stationId(), json(dto), changedAt);
        log.info("ref_changes v{}: trạm {} enabled={}", v, dto.stationId(), dto.enabled());
    }

    private String json(Object dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Không tuần tự hóa được dữ liệu tham chiếu", e);
        }
    }
}
