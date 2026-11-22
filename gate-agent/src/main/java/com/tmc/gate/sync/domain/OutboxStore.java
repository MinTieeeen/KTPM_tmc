package com.tmc.gate.sync.domain;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Đọc và đánh dấu outbox phía G4 (ADR-03). */
public interface OutboxStore {

    /** Các bản ghi PENDING theo {@code seq} tăng dần, tối đa {@code limit}. */
    List<OutboxRecord> fetchPending(int limit);

    /** PENDING → SYNCED, chỉ với các {@code eventId} được nêu. */
    void markSynced(Collection<UUID> eventIds, Instant at);

    /** PENDING → REJECTED kèm lý do C1 trả về; không gửi lại. */
    void markRejected(Map<UUID, String> reasons, Instant at);

    long countPending();
}
