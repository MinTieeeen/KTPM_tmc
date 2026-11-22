package com.tmc.contract.snapshot;

import java.util.List;

/**
 * Phần thay đổi của dữ liệu tham chiếu kể từ {@code fromVersion} (SRS-SC-FR-05).
 * {@code since=0} nghĩa là toàn bộ.
 */
public record SnapshotDelta(
        long fromVersion,
        long toVersion,
        List<CredentialEntryDto> credentials,
        List<PublicKeyDto> publicKeys,
        List<StationConfigDto> stations,
        GateSettingsDto settings) {

    public SnapshotDelta {
        credentials = credentials == null ? List.of() : List.copyOf(credentials);
        publicKeys = publicKeys == null ? List.of() : List.copyOf(publicKeys);
        stations = stations == null ? List.of() : List.copyOf(stations);
    }
}
