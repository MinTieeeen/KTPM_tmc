package com.tmc.sim.turnstile.application;

import java.time.Instant;
import java.util.UUID;

/**
 * Kết quả lệnh mở.
 *
 * @param duplicate {@code true} khi {@code requestId} đã được thực hiện trước đó (không mở lần 2)
 * @param at        thời điểm cổng mở (lần đầu)
 */
public record OpenResult(UUID requestId, boolean executed, boolean duplicate, Instant at) {
}
