package com.tmc.central.station.api;

import java.time.Instant;

/**
 * Sự kiện: cấu hình trạm thay đổi (FR-06, INV-03); C1 nghe để gửi xuống gate. MVP chưa có thao tác thay đổi trạm
 * nên chưa có nơi phát; dữ liệu ban đầu C1 lấy qua {@link StationApi#allStations()}.
 */
public record StationConfigChanged(String stationId, String factoryId, String direction, boolean enabled,
                                   String turnstileId, Instant changedAt) {
}
