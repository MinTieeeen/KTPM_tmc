package com.tmc.central.ingestion.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Tham số C1 (mục 6.6): {@code tmc.central.sync.max-batch-size} và {@code tmc.central.settings.offline-threshold}
 * (gửi xuống gate dạng phút, thay C9). Các khóa khác dưới {@code tmc.central} thuộc module khác và được bỏ qua.
 */
@ConfigurationProperties("tmc.central")
public record IngestionProperties(@DefaultValue Sync sync, @DefaultValue Settings settings) {

    public record Sync(@DefaultValue("200") int maxBatchSize) {
    }

    public record Settings(@DefaultValue("8h") Duration offlineThreshold) {
    }
}
