package com.tmc.gate.access.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Tham số của G1 dưới {@code tmc.gate.*} (mục 6.8, DP-17). Chỉ đọc các khóa G1 cần; các khóa khác
 * dưới cùng tiền tố thuộc module khác và được bỏ qua.
 */
@ConfigurationProperties("tmc.gate")
public record AccessProperties(
        @DefaultValue("gate-bd") String gateId,
        @DefaultValue("BD") String factoryId,
        @DefaultValue Qr qr,
        @DefaultValue Turnstile turnstile,
        @DefaultValue Access access) {

    /** @param clockSkew độ lệch đồng hồ cho phép khi kiểm {@code iat}/{@code exp} (ADR-02) */
    public record Qr(@DefaultValue("30s") Duration clockSkew) {
    }

    /**
     * @param openTimeout   giới hạn chờ lệnh mở (ngân sách thời gian, mục 6.3 báo cáo)
     * @param statusTimeout giới hạn chờ kiểm tra trạng thái lệnh (NFR-REL-02)
     */
    public record Turnstile(
            @DefaultValue("800ms") Duration openTimeout,
            @DefaultValue("200ms") Duration statusTimeout) {
    }

    /**
     * @param stuckPendingAfter lượt PENDING lâu hơn ngưỡng này được đếm vào {@code pendingReconcile}
     * @param onlineWithin      {@code online = now − lastSuccessfulSyncAt ≤ onlineWithin}
     */
    public record Access(
            @DefaultValue("5s") Duration stuckPendingAfter,
            @DefaultValue("30s") Duration onlineWithin) {
    }
}
