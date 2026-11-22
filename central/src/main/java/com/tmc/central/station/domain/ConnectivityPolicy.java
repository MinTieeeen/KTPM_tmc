package com.tmc.central.station.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/** Quy tắc online/offline của gate (FR-05): online khi heartbeat mới nhất được nhận trong {@code offlineAfter}. */
public final class ConnectivityPolicy {

    private final Duration offlineAfter;

    public ConnectivityPolicy(Duration offlineAfter) {
        this.offlineAfter = Objects.requireNonNull(offlineAfter, "offlineAfter");
    }

    /** @param lastReceivedAt {@code null} khi chưa có heartbeat → offline */
    public boolean isOnline(Instant lastReceivedAt, Instant now) {
        return lastReceivedAt != null && Duration.between(lastReceivedAt, now).compareTo(offlineAfter) <= 0;
    }
}
