package com.tmc.gate.access.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Quy tắc cho qua tại cổng – hàm thuần {@code ScanContext → Decision} (mục 6.5).
 * Quy tắc 1, 2 (định dạng, chữ ký, thời hạn) đã được {@link QrTokenVerifier} kiểm; lớp này kiểm quy tắc
 * 3 → 7 theo thứ tự cố định và dừng ở quy tắc đầu tiên vi phạm. Quy tắc trạm đặt trước quy tắc credential
 * để trạm bị tắt không làm lộ thông tin credential (INV-03).
 */
public final class AccessRules {

    public Decision evaluate(ScanContext ctx) {
        // 3. Trạm tồn tại và đang bật (INV-03)
        if (ctx.station().isEmpty()) {
            return deny(RejectReason.UNKNOWN_STATION);
        }
        StationConfig station = ctx.station().get();
        if (!station.enabled()) {
            return deny(RejectReason.STATION_DISABLED);
        }
        Direction direction = station.direction();

        // 4. Chiều vào khi mất kết nối quá ngưỡng (FR-17, DP-08) – chiều ra luôn được xét tiếp
        if (direction == Direction.IN
                && isOfflineThresholdExceeded(ctx.lastSuccessfulSyncAt(), ctx.offlineThreshold(), ctx.now())) {
            return deny(RejectReason.OFFLINE_THRESHOLD_EXCEEDED);
        }

        // 5. Credential có trong bản sao, ACTIVE, nhân viên đang hoạt động (INV-01, INV-02)
        if (ctx.credential().isEmpty()) {
            return deny(RejectReason.CREDENTIAL_NOT_FOUND);
        }
        CredentialEntry credential = ctx.credential().get();
        if (credential.status() != CredentialStatus.ACTIVE) {
            return deny(RejectReason.CREDENTIAL_REVOKED);
        }
        if (!credential.employeeActive()) {
            return deny(RejectReason.EMPLOYEE_INACTIVE);
        }

        // 6. Nonce chưa dùng (NFR-SEC-05, R-06)
        if (ctx.nonceUsed()) {
            return deny(RejectReason.NONCE_REUSED);
        }

        // 7. Anti-passback: VÀO sau VÀO bị từ chối (INV-04); RA sau RA cho qua kèm cảnh báo
        Optional<Direction> last = ctx.lastAcceptedDirection();
        String warning = null;
        if (last.isPresent() && last.get() == direction) {
            if (direction == Direction.IN) {
                return deny(RejectReason.ANTI_PASSBACK);
            }
            warning = Decision.WARNING_OUT_AFTER_OUT;
        }

        return new Decision.Allow(credential.employeeCode(), direction, credential.credentialId(), warning);
    }

    /**
     * Mất kết nối quá ngưỡng: chưa từng đồng bộ (D5 – an toàn khi thiếu thông tin) hoặc
     * {@code now − lastSuccessfulSyncAt > threshold}.
     */
    public static boolean isOfflineThresholdExceeded(Optional<Instant> lastSuccessfulSyncAt,
                                                     Duration threshold, Instant now) {
        return lastSuccessfulSyncAt
                .map(last -> Duration.between(last, now).compareTo(threshold) > 0)
                .orElse(true);
    }

    private static Decision deny(RejectReason reason) {
        return new Decision.Deny(reason);
    }
}
