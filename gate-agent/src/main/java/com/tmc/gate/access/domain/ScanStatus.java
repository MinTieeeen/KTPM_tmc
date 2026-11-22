package com.tmc.gate.access.domain;

/** Trạng thái vòng đời của một lượt quét (mục 6.6). */
public enum ScanStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    GATE_FAILED
}
