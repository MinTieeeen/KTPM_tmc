package com.tmc.central.iam.api;

import java.time.Instant;

/**
 * Kết quả đăng nhập.
 *
 * @param employeeCode chỉ có với EMPLOYEE (bị bỏ khỏi JSON khi {@code null})
 */
public record LoginResponse(String accessToken, Instant expiresAt, String role, String employeeCode) {

    @Override
    public String toString() {
        return "LoginResponse[role=" + role + ", employeeCode=" + employeeCode + ", expiresAt=" + expiresAt + "]";
    }
}
