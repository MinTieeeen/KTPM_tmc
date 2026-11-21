package com.tmc.gate.sync.domain;

import java.time.Instant;
import java.util.Optional;

/** Mốc đồng bộ do G4 sở hữu ({@code snapshot_version}, {@code last_success_at}). */
public interface SyncStateStore {

    /** Version snapshot đã áp; {@code 0} khi chưa từng kéo. */
    long snapshotVersion();

    /** Ghi nhận một lời gọi Central thành công (push, pull hoặc heartbeat). */
    void recordSuccessfulSync(Instant at);

    Optional<Instant> lastSuccessfulSyncAt();
}
