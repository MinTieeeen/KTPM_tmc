package com.tmc.central.station.api;

/**
 * Cấu hình một trạm cổng.
 *
 * @param direction {@code IN} | {@code OUT}
 */
public record StationInfo(String stationId, String factoryId, String direction, boolean enabled, String turnstileId) {
}
