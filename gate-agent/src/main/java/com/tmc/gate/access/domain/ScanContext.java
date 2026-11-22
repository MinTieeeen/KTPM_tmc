package com.tmc.gate.access.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Toàn bộ dữ liệu đầu vào của {@link AccessRules}, được use case đọc sẵn từ các port.
 *
 * @param station              cấu hình trạm, rỗng khi trạm không có trong bản sao
 * @param credential           credential của token, rỗng khi không có trong bản sao
 * @param nonceUsed            nonce của token đã được dùng trước đó
 * @param lastAcceptedDirection chiều của lượt được chấp nhận gần nhất của nhân viên (presence)
 * @param lastSuccessfulSyncAt lần đồng bộ thành công gần nhất với Central, rỗng nếu chưa từng
 * @param offlineThreshold     ngưỡng mất kết nối
 */
public record ScanContext(
        VerifiedToken token,
        Optional<StationConfig> station,
        Optional<CredentialEntry> credential,
        boolean nonceUsed,
        Optional<Direction> lastAcceptedDirection,
        Optional<Instant> lastSuccessfulSyncAt,
        Duration offlineThreshold,
        Instant now) {

    public ScanContext {
        Objects.requireNonNull(token, "token");
        Objects.requireNonNull(station, "station");
        Objects.requireNonNull(credential, "credential");
        Objects.requireNonNull(lastAcceptedDirection, "lastAcceptedDirection");
        Objects.requireNonNull(lastSuccessfulSyncAt, "lastSuccessfulSyncAt");
        Objects.requireNonNull(offlineThreshold, "offlineThreshold");
        Objects.requireNonNull(now, "now");
    }
}
