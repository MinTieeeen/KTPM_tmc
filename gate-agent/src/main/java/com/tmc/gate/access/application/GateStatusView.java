package com.tmc.gate.access.application;

import com.tmc.gate.access.domain.TurnstileHealth;

import java.time.Instant;
import java.util.Map;

/**
 * Trạng thái của gate cho kiosk (FR-09, NFR-AVL-04).
 *
 * @param lastSyncAt       {@code null} khi chưa từng đồng bộ
 * @param pendingReconcile số lượt PENDING kẹt lâu hơn ngưỡng {@code stuck-pending-after}
 * @param turnstiles       stationId → sức khỏe cổng xoay
 */
public record GateStatusView(
        String gateId,
        boolean online,
        Instant lastSyncAt,
        long outboxDepth,
        boolean offlineThresholdExceeded,
        long pendingReconcile,
        Map<String, TurnstileHealth> turnstiles) {
}
