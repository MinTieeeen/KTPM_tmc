package com.tmc.central.ingestion.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmc.central.ingestion.domain.RefChange;
import com.tmc.central.ingestion.domain.RefChangeStore;
import com.tmc.contract.snapshot.CredentialEntryDto;
import com.tmc.contract.snapshot.GateSettingsDto;
import com.tmc.contract.snapshot.PublicKeyDto;
import com.tmc.contract.snapshot.SnapshotDelta;
import com.tmc.contract.snapshot.StationConfigDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Dựng {@link SnapshotDelta} từ {@code ref_changes} (FR-07): bản mới nhất theo {@code (kind, key)} của các dòng
 * {@code version > since}. {@code since} âm hoặc lớn hơn version hiện tại (ví dụ CSDL Central bị dựng lại) → trả
 * delta đầy đủ từ 0 để gate tự đồng bộ lại.
 */
@Service
public class GetSnapshotDeltaUseCase {

    private final RefChangeStore refChanges;
    private final ObjectMapper objectMapper;
    private final IngestionProperties props;

    public GetSnapshotDeltaUseCase(RefChangeStore refChanges, ObjectMapper objectMapper, IngestionProperties props) {
        this.refChanges = refChanges;
        this.objectMapper = objectMapper;
        this.props = props;
    }

    @Transactional(readOnly = true)
    public SnapshotDelta deltaSince(long since) {
        long current = refChanges.currentVersion();
        long from = (since < 0 || since > current) ? 0 : since;

        List<CredentialEntryDto> credentials = new ArrayList<>();
        List<PublicKeyDto> publicKeys = new ArrayList<>();
        List<StationConfigDto> stations = new ArrayList<>();
        long toVersion = from;
        for (RefChange change : refChanges.latestSince(from)) {
            toVersion = Math.max(toVersion, change.version());
            switch (change.kind()) {
                case CREDENTIAL -> credentials.add(read(change, CredentialEntryDto.class));
                case PUBLIC_KEY -> publicKeys.add(read(change, PublicKeyDto.class));
                case STATION -> stations.add(read(change, StationConfigDto.class));
            }
        }
        GateSettingsDto settings = new GateSettingsDto(
                Math.toIntExact(props.settings().offlineThreshold().toMinutes()));
        return new SnapshotDelta(from, toVersion, credentials, publicKeys, stations, settings);
    }

    private <T> T read(RefChange change, Class<T> type) {
        try {
            return objectMapper.readValue(change.payloadJson(), type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("ref_changes version " + change.version() + " hỏng", e);
        }
    }
}
