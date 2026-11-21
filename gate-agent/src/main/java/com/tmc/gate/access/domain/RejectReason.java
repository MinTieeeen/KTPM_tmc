package com.tmc.gate.access.domain;

/**
 * Lý do một lượt quét không được chấp nhận (bảng 7.2 đặc tả M2, mục 10.3).
 * Ba giá trị cuối là lỗi cổng xoay – kết quả {@link ScanStatus#GATE_FAILED}, không phải REJECTED.
 */
public enum RejectReason {
    // Quy tắc 1, 2 – xác thực token (QrTokenVerifier)
    INVALID_FORMAT,
    UNKNOWN_KEY,
    BAD_SIGNATURE,
    EXPIRED,
    NOT_YET_VALID,
    // Quy tắc 3 – trạm (INV-03)
    UNKNOWN_STATION,
    STATION_DISABLED,
    // Quy tắc 4 – mất kết nối quá ngưỡng (FR-17)
    OFFLINE_THRESHOLD_EXCEEDED,
    // Quy tắc 5 – credential (INV-01, INV-02)
    CREDENTIAL_NOT_FOUND,
    CREDENTIAL_REVOKED,
    EMPLOYEE_INACTIVE,
    // Quy tắc 6, 7
    NONCE_REUSED,
    ANTI_PASSBACK,
    // Lỗi cổng xoay
    GATE_TIMEOUT,
    GATE_ERROR,
    CIRCUIT_OPEN;

    public boolean isGateFailure() {
        return this == GATE_TIMEOUT || this == GATE_ERROR || this == CIRCUIT_OPEN;
    }
}
