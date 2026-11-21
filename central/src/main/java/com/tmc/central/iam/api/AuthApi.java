package com.tmc.central.iam.api;

/** Thông tin người dùng hiện tại cho các module khác (FR-03, DP-15). */
public interface AuthApi {

    CurrentUser currentUser();

    /**
     * Mã nhân viên của người dùng hiện tại, lấy từ claim {@code employeeCode} của JWT.
     *
     * @throws org.springframework.security.access.AccessDeniedException người dùng không phải EMPLOYEE
     */
    String currentEmployeeCode();
}
