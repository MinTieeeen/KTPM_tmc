package com.tmc.central.employee.api;

import java.time.Instant;
import java.util.UUID;

/**
 * Trạng thái một credential gửi xuống gate – chỉ các trường cần thiết, không họ tên (NFR-04, DP-12).
 *
 * @param status    {@code ACTIVE} | {@code REVOKED}
 * @param updatedAt thời điểm thay đổi gần nhất (thu hồi hoặc cấp)
 */
public record CredentialState(UUID credentialId, String employeeCode, String status, boolean employeeActive,
                              Instant updatedAt) {
}
