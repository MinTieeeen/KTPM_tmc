package com.tmc.central.employee.domain;

import java.time.Instant;

/** Mã QR đã ký ({@code TMC1.<payload>.<signature>}). */
public record SignedQrToken(String raw, Instant expiresAt) {

    /** Không để lộ chuỗi QR trong log (NFR-PRI-03). */
    @Override
    public String toString() {
        return "SignedQrToken[expiresAt=" + expiresAt + "]";
    }
}
