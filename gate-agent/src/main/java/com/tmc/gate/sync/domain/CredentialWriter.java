package com.tmc.gate.sync.domain;

import com.tmc.contract.snapshot.SnapshotDelta;

/** Áp delta dữ liệu tham chiếu vào bản sao cục bộ (G4 ghi, G1 chỉ đọc – ISP). */
public interface CredentialWriter {

    /**
     * Một transaction: upsert credential, khóa công khai, trạm, tham số và {@code snapshot_version}.
     * Bỏ qua delta có {@code fromVersion} khác version hiện tại (trừ {@code fromVersion = 0}).
     * Idempotent nếu áp lại cùng delta.
     *
     * @return {@code true} nếu delta đã được áp
     */
    boolean applyDelta(SnapshotDelta delta);
}
