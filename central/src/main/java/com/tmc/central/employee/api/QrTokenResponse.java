package com.tmc.central.employee.api;

import java.time.Instant;

/**
 * Body của {@code GET /portal/qr-token}.
 *
 * @param refreshAfterSec Portal lấy mã mới sau số giây này (30 s)
 */
public record QrTokenResponse(String raw, Instant expiresAt, long refreshAfterSec) {

    /** Không để lộ chuỗi QR trong log (NFR-PRI-03). */
    @Override
    public String toString() {
        return "QrTokenResponse[expiresAt=" + expiresAt + "]";
    }
}
