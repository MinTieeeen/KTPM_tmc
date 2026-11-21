package com.tmc.central.employee.api;

import java.time.Instant;
import java.util.UUID;

/**
 * Một dòng của bảng nhân viên trên Admin. Ba trường credential vắng khi nhân viên chưa từng được cấp.
 *
 * @param credentialStatus {@code ACTIVE} | {@code REVOKED} của credential mới nhất
 */
public record EmployeeView(
        String code,
        String fullName,
        String departmentId,
        String departmentName,
        String factoryId,
        boolean active,
        UUID credentialId,
        String credentialStatus,
        Instant issuedAt) {
}
