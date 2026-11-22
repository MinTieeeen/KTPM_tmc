package com.tmc.gate.sync.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Tham số G4 dưới {@code tmc.gate.sync.*} (mục 6.6). {@code pullInterval} phải ≤ 60 s để thu hồi QR có hiệu lực
 * trong 60 s (NFR-SEC-06) – sai thì không khởi động (fail fast).
 */
@ConfigurationProperties("tmc.gate.sync")
public record SyncProperties(
        @DefaultValue("2s") Duration pushInterval,
        @DefaultValue("200") int pushBatchSize,
        @DefaultValue("15s") Duration pullInterval,
        @DefaultValue("10s") Duration heartbeatInterval,
        @DefaultValue("60s") Duration nonceCleanupInterval,
        @DefaultValue("8h") Duration defaultOfflineThreshold) {

    private static final Duration MAX_PULL_INTERVAL = Duration.ofSeconds(60);
    /** Số lô tối đa đẩy trong một chu kỳ khi outbox tồn nhiều (FR-01). */
    public static final int MAX_BATCHES_PER_CYCLE = 10;

    public SyncProperties {
        if (pullInterval.compareTo(MAX_PULL_INTERVAL) > 0) {
            throw new IllegalStateException("tmc.gate.sync.pull-interval phải ≤ 60s (NFR-SEC-06), hiện là " + pullInterval);
        }
        if (pushBatchSize < 1 || pushBatchSize > 200) {
            throw new IllegalStateException("tmc.gate.sync.push-batch-size phải trong [1, 200]");
        }
    }
}
