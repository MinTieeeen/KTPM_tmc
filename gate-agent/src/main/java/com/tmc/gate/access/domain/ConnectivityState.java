package com.tmc.gate.access.domain;

import java.time.Instant;
import java.util.Optional;

/** Trạng thái kết nối với Central, dùng cho quy tắc mất kết nối quá ngưỡng (FR-17). */
public interface ConnectivityState {

    /** Rỗng nếu gate chưa từng đồng bộ thành công. */
    Optional<Instant> lastSuccessfulSyncAt();

    /** Ngưỡng lấy từ snapshot; nếu chưa có thì lấy từ cấu hình. */
    int offlineThresholdMinutes();
}
