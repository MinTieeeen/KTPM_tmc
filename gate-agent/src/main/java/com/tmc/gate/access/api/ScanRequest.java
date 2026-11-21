package com.tmc.gate.access.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body của {@code POST /scan}. */
public record ScanRequest(
        @NotBlank String stationId,
        @NotBlank @Size(max = 1024) String raw) {

    /** Không để lộ chuỗi QR trong log (NFR-PRI-03). */
    @Override
    public String toString() {
        return "ScanRequest[stationId=" + stationId + "]";
    }
}
