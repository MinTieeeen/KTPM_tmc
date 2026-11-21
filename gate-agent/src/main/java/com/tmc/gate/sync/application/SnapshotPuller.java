package com.tmc.gate.sync.application;

import com.tmc.contract.snapshot.SnapshotDelta;
import com.tmc.gate.sync.domain.CredentialWriter;
import com.tmc.gate.sync.domain.SyncClient;
import com.tmc.gate.sync.domain.SyncStateStore;
import com.tmc.gate.sync.domain.SyncUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;

/**
 * Kéo delta dữ liệu tham chiếu (FR-04, FR-09): {@code GET /sync/snapshot?since={snapshot_version}} rồi áp bằng
 * {@link CredentialWriter#applyDelta} trong một transaction. Chạy ngay khi khởi động, sau đó mỗi
 * {@code pull-interval} (≤ 60 s – NFR-SEC-06).
 */
@Component
public class SnapshotPuller {

    private static final Logger log = LoggerFactory.getLogger(SnapshotPuller.class);

    private final SyncClient client;
    private final CredentialWriter writer;
    private final SyncStateStore state;
    private final Clock clock;

    public SnapshotPuller(SyncClient client, CredentialWriter writer, SyncStateStore state, Clock clock) {
        this.client = client;
        this.writer = writer;
        this.state = state;
        this.clock = clock;
    }

    @Scheduled(initialDelay = 0, fixedDelayString = "${tmc.gate.sync.pull-interval:15s}")
    public void scheduledPull() {
        try {
            pullOnce();
        } catch (RuntimeException e) {
            log.error("Lỗi không mong đợi khi kéo snapshot", e);
        }
    }

    /** @return {@code true} nếu đã kéo được delta từ Central */
    public boolean pullOnce() {
        long version = state.snapshotVersion();
        SnapshotDelta delta;
        try {
            delta = client.pull(version);
        } catch (SyncUnavailableException e) {
            log.debug("Chưa kéo được snapshot: {}", e.getMessage());
            return false;
        }
        // Có thay đổi mới, hoặc delta đầy đủ (fromVersion=0: lần đầu, hoặc Central yêu cầu đồng bộ lại vì version
        // của gate không hợp lệ). applyDelta idempotent nên áp lại delta đầy đủ là an toàn.
        if (delta.toVersion() > version || delta.fromVersion() == 0) {
            writer.applyDelta(delta);
        }
        state.recordSuccessfulSync(clock.instant());
        return true;
    }
}
