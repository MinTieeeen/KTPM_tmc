package com.tmc.central.station.api;

import java.time.Instant;

/**
 * Một dòng của tab Trạm trên Admin. Các trường kết nối lấy từ heartbeat mới nhất của gate thuộc nhà máy của trạm;
 * vắng khi gate chưa từng gửi heartbeat.
 */
public record StationView(
        String stationId,
        String factoryId,
        String direction,
        boolean enabled,
        String gateId,
        boolean online,
        Instant lastHeartbeatAt,
        Long outboxDepth,
        Instant lastSyncAt,
        String turnstileStatus) {
}
