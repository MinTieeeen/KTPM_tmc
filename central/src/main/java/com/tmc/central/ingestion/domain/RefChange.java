package com.tmc.central.ingestion.domain;

import java.time.Instant;

/**
 * Một dòng {@code ref_changes}.
 *
 * @param version     số tăng dần (BIGSERIAL); {@code 0} khi chưa lưu
 * @param payloadJson JSON của DTO {@code sync-contract} tương ứng với {@code kind}
 */
public record RefChange(long version, RefChangeKind kind, String key, String payloadJson, Instant changedAt) {
}
