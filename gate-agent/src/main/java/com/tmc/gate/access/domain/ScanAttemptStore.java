package com.tmc.gate.access.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Lưu vòng đời lượt quét; mỗi thao tác ghi là một transaction (transactional outbox, ADR-03). */
public interface ScanAttemptStore {

    /**
     * Một transaction: thêm lượt PENDING và đánh dấu nonce đã dùng tới {@code nonceExpiresAt}.
     *
     * @throws NonceAlreadyUsedException nonce đã có – không ghi gì
     */
    void recordPending(ScanAttempt attempt, String nonce, Instant nonceExpiresAt);

    /** Một transaction: thêm lượt REJECTED + 1 bản ghi outbox (INV-07). */
    void recordRejected(ScanAttempt attempt);

    /**
     * Một transaction: PENDING → GATE_FAILED + 1 bản ghi outbox.
     *
     * @throws IllegalStateException lượt không còn PENDING
     */
    void markGateFailed(UUID eventId, RejectReason reason, Instant at);

    /** Mọi lượt còn PENDING (đối soát sau crash). */
    List<ScanAttempt> findPending();
}
