package com.tmc.gate.turnstile.infrastructure;

/** Hành vi của {@link FakeTurnstileGateway} (bảng 6.5 spec gate-agent-turnstile). */
public enum FakeBehavior {
    /** Sau {@code latency} → OPENED; {@code status} → EXECUTED. */
    OPEN,
    /** Sau {@code timeout} → TIMEOUT; {@code status} → NOT_EXECUTED. */
    TIMEOUT,
    /** Ngay → ERROR; {@code status} → NOT_EXECUTED. */
    ERROR,
    /** Cổng mở nhưng trả lời quá muộn: sau {@code timeout} → TIMEOUT; {@code status} → EXECUTED. */
    EXECUTE_THEN_TIMEOUT
}
