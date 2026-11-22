package com.tmc.sim.turnstile.api;

import com.tmc.sim.turnstile.application.OpenResult;
import com.tmc.sim.turnstile.domain.Behavior;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** DTO HTTP của bộ giả lập (mục 5). */
final class SimDtos {

    private SimDtos() {
    }

    /** @throws IllegalArgumentException thiếu {@code requestId} */
    record OpenRequest(UUID requestId) {
        OpenRequest {
            if (requestId == null) {
                throw new IllegalArgumentException("requestId là bắt buộc");
            }
        }
    }

    record OpenResponse(UUID requestId, boolean executed, boolean duplicate, Instant at) {
        static OpenResponse from(OpenResult r) {
            return new OpenResponse(r.requestId(), r.executed(), r.duplicate(), r.at());
        }
    }

    /** {@code at} vắng khi chưa thực hiện. */
    record CommandStatusResponse(boolean executed, Instant at) {
    }

    record TurnstileStatusResponse(String turnstileId, String status, long openCount) {
    }

    /** Cập nhật từng phần: trường {@code null} giữ nguyên. {@code latencyMs = [min, max]}. */
    record BehaviorRequest(List<Long> latencyMs, Double failureRate, String mode) {
        BehaviorRequest {
            if (latencyMs != null && (latencyMs.size() != 2 || latencyMs.contains(null))) {
                throw new IllegalArgumentException("latencyMs phải là [min, max]");
            }
        }
    }

    record BehaviorResponse(List<Long> latencyMs, double failureRate, String mode) {
        static BehaviorResponse from(Behavior b) {
            return new BehaviorResponse(List.of(b.latencyMinMs(), b.latencyMaxMs()), b.failureRate(), b.mode().name());
        }
    }
}
