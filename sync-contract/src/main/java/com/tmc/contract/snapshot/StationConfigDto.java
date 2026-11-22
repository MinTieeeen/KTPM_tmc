package com.tmc.contract.snapshot;

/**
 * Cấu hình một trạm cổng.
 *
 * @param direction {@code IN} | {@code OUT}
 */
public record StationConfigDto(
        String stationId,
        String factoryId,
        String direction,
        boolean enabled,
        String turnstileId) {
}
