package com.tmc.central.employee.api;

import java.time.Instant;
import java.util.UUID;

/**
 * Sự kiện: credential đổi trạng thái (cấp hoặc thu hồi). Được Spring Modulith lưu và giao sau commit (FR-07,
 * INV-02); C1 nghe để ghi delta cho gate.
 *
 * @param status {@code ACTIVE} | {@code REVOKED}
 */
public record QrCredentialChanged(UUID credentialId, String employeeCode, String status, boolean employeeActive,
                                  Instant changedAt) {
}
