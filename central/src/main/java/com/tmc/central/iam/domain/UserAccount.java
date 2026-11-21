package com.tmc.central.iam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Tài khoản đăng nhập. Liên kết với nhân viên bằng chuỗi {@code employeeCode}, không có khóa ngoại sang module
 * {@code employee} (DP-04). Một vai trò mỗi tài khoản (D6).
 */
@Entity
@Table(name = "user_accounts")
public class UserAccount {

    @Id
    @Column(name = "username", length = 32)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 16)
    private Role role;

    @Column(name = "employee_code", length = 16)
    private String employeeCode;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected UserAccount() {
        // JPA
    }

    /** Tài khoản bị vô hiệu hóa không đăng nhập được (NFR-SEC-04). */
    public boolean canLogin() {
        return enabled;
    }

    public String username() {
        return username;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public Role role() {
        return role;
    }

    /** {@code null} với ADMIN. */
    public String employeeCode() {
        return employeeCode;
    }

    public boolean enabled() {
        return enabled;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
