package com.tmc.gate.access.application;

import java.time.Duration;
import java.util.Objects;

/**
 * Tham số vận hành mà use case của G1 cần (DP-17). Được dựng từ cấu hình {@code tmc.gate.*} ở tầng
 * infrastructure, để tầng application không phụ thuộc Spring Boot properties.
 */
public record AccessSettings(
        Duration clockSkew,
        Duration openTimeout,
        Duration statusTimeout,
        Duration stuckPendingAfter,
        Duration onlineWithin) {

    public AccessSettings {
        Objects.requireNonNull(clockSkew, "clockSkew");
        Objects.requireNonNull(openTimeout, "openTimeout");
        Objects.requireNonNull(statusTimeout, "statusTimeout");
        Objects.requireNonNull(stuckPendingAfter, "stuckPendingAfter");
        Objects.requireNonNull(onlineWithin, "onlineWithin");
    }
}
