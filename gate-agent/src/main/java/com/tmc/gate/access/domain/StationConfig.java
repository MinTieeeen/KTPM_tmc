package com.tmc.gate.access.domain;

/** Cấu hình một trạm cổng: mỗi trạm gắn đúng một cổng xoay và một chiều cố định. */
public record StationConfig(
        String stationId,
        String factoryId,
        Direction direction,
        boolean enabled,
        String turnstileId) {
}
