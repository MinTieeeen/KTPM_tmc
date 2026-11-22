package com.tmc.sim.turnstile.domain;

import java.util.Objects;

/**
 * Hành vi hiện tại của bộ giả lập (bất biến).
 *
 * @throws IllegalArgumentException khi {@code latencyMinMs > latencyMaxMs}, độ trễ âm hoặc
 *                                  {@code failureRate ∉ [0, 1]}
 */
public record Behavior(long latencyMinMs, long latencyMaxMs, double failureRate, BehaviorMode mode) {

    public Behavior {
        Objects.requireNonNull(mode, "mode");
        if (latencyMinMs < 0 || latencyMaxMs < 0) {
            throw new IllegalArgumentException("Độ trễ không được âm");
        }
        if (latencyMinMs > latencyMaxMs) {
            throw new IllegalArgumentException("latencyMs: min > max");
        }
        if (Double.isNaN(failureRate) || failureRate < 0 || failureRate > 1) {
            throw new IllegalArgumentException("failureRate phải trong [0, 1]");
        }
    }
}
