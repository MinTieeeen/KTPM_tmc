package com.tmc.gate.access.domain;

/** Sức khỏe gần nhất của một cổng xoay. */
public enum TurnstileHealth {
    /** Lệnh gần nhất thành công hoặc chưa gửi lệnh nào. */
    UP,
    /** Lệnh gần nhất thất bại. */
    DOWN,
    /** Circuit breaker đang mở: tạm ngừng gửi lệnh. */
    CIRCUIT_OPEN
}
