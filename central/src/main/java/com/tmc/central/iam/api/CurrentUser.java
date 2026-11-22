package com.tmc.central.iam.api;

/**
 * Người dùng đã xác thực. Cũng là body của {@code GET /auth/me}.
 *
 * @param role         {@code "EMPLOYEE"} | {@code "ADMIN"} – kiểu String để không lộ enum của tầng domain
 * @param employeeCode chỉ có với EMPLOYEE
 */
public record CurrentUser(String username, String role, String employeeCode) {

    public static final String ROLE_EMPLOYEE = "EMPLOYEE";
    public static final String ROLE_ADMIN = "ADMIN";
}
