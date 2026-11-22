package com.tmc.gate.sync.domain;

import com.tmc.contract.event.BatchAck;
import com.tmc.contract.event.EventBatch;
import com.tmc.contract.heartbeat.Heartbeat;
import com.tmc.contract.snapshot.SnapshotDelta;

/**
 * Lời gọi SyncAPI của Central (cài đặt: RestSyncClient). Hợp đồng: lỗi mạng, timeout, 5xx, 4xx hoặc giả lập offline
 * → {@link SyncUnavailableException}; không bao giờ trả {@code null}.
 */
public interface SyncClient {

    BatchAck push(EventBatch batch);

    SnapshotDelta pull(long sinceVersion);

    void heartbeat(Heartbeat heartbeat);
}
