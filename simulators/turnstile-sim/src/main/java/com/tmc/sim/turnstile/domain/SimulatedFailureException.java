package com.tmc.sim.turnstile.domain;

/** Lỗi giả lập theo chế độ hiện tại: {@code ERROR} → 500, {@code TIMEOUT} → 504. */
public class SimulatedFailureException extends RuntimeException {

    public enum Kind { ERROR, TIMEOUT }

    private final Kind kind;

    public SimulatedFailureException(Kind kind, String message) {
        super(message);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }
}
