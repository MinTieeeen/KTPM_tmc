package com.tmc.gate.access.domain;

/** Kết quả lệnh mở cổng xoay ({@link TurnstileGateway#open}). */
public enum GateResult {
    OPENED,
    TIMEOUT,
    ERROR,
    CIRCUIT_OPEN
}
