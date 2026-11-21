package com.tmc.gate.access.domain;

import java.util.UUID;

/**
 * Lệnh mở cổng xoay.
 *
 * @param requestId bằng {@code eventId} của lượt quét – cổng dùng để chống thực hiện lặp
 */
public record OpenCommand(String turnstileId, UUID requestId, String stationId) {
}
