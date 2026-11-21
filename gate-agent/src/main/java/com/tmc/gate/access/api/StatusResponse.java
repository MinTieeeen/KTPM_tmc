package com.tmc.gate.access.api;

import com.tmc.gate.access.application.GateStatusView;

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

/** Phản hồi của {@code GET /status}. */
public record StatusResponse(
        String gateId,
        boolean online,
        Instant lastSyncAt,
        long outboxDepth,
        boolean offlineThresholdExceeded,
        long pendingReconcile,
        Map<String, String> turnstiles) {

    static StatusResponse from(GateStatusView v) {
        Map<String, String> turnstiles = new TreeMap<>();
        v.turnstiles().forEach((stationId, health) -> turnstiles.put(stationId, health.name()));
        return new StatusResponse(v.gateId(), v.online(), v.lastSyncAt(), v.outboxDepth(),
                v.offlineThresholdExceeded(), v.pendingReconcile(), turnstiles);
    }
}
