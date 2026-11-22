package com.tmc.gate.access.domain;

import java.time.Duration;
import java.util.UUID;

/**
 * Điều khiển cổng xoay (cài đặt: HttpTurnstileAdapter, FakeTurnstileGateway).
 * <p>Hợp đồng LSP: luôn trả kết quả trong {@code timeout} (+ ≤ 50 ms), không ném ngoại lệ.
 */
public interface TurnstileGateway {

    /** Gửi lệnh mở; {@code cmd.requestId} dùng để cổng chống thực hiện lặp. */
    GateResult open(OpenCommand cmd, Duration timeout);

    /** Hỏi lệnh {@code requestId} đã được thực hiện chưa; không chắc chắn → {@link CommandStatus#UNKNOWN}. */
    CommandStatus status(String turnstileId, UUID requestId, Duration timeout);
}
