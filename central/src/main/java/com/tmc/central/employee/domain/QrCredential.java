package com.tmc.central.employee.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Credential QR của nhân viên (mục 6.5): {@code issue()} → ACTIVE ─{@code revoke(reason)}→ REVOKED (cuối).
 * Tối đa một ACTIVE mỗi nhân viên – bảo đảm bằng unique index một phần {@code ux_one_active} (INV-01).
 * Credential cũ không bị xóa.
 */
@Entity
@Table(name = "qr_credentials")
public class QrCredential {

    /** Lý do thu hồi tự động khi cấp lại (FR-04). */
    public static final String REASON_REISSUED = "REISSUED";

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "employee_code", nullable = false, length = 16)
    private String employeeCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 8)
    private CredentialStatus status;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoke_reason", length = 200)
    private String revokeReason;

    protected QrCredential() {
        // JPA
    }

    private QrCredential(UUID id, String employeeCode, Instant issuedAt) {
        this.id = id;
        this.employeeCode = employeeCode;
        this.status = CredentialStatus.ACTIVE;
        this.issuedAt = issuedAt;
    }

    public static QrCredential issue(String employeeCode, Instant now) {
        Objects.requireNonNull(employeeCode, "employeeCode");
        Objects.requireNonNull(now, "now");
        return new QrCredential(UUID.randomUUID(), employeeCode, now);
    }

    /** @throws IllegalStateException credential đã bị thu hồi */
    public void revoke(String reason, Instant now) {
        if (status != CredentialStatus.ACTIVE) {
            throw new IllegalStateException("Credential " + id + " đã bị thu hồi");
        }
        this.status = CredentialStatus.REVOKED;
        this.revokedAt = Objects.requireNonNull(now, "now");
        this.revokeReason = Objects.requireNonNull(reason, "reason");
    }

    public boolean isActive() {
        return status == CredentialStatus.ACTIVE;
    }

    /** Thời điểm thay đổi gần nhất: thu hồi nếu đã thu hồi, ngược lại là lúc cấp. */
    public Instant updatedAt() {
        return revokedAt != null ? revokedAt : issuedAt;
    }

    public UUID id() {
        return id;
    }

    public String employeeCode() {
        return employeeCode;
    }

    public CredentialStatus status() {
        return status;
    }

    public Instant issuedAt() {
        return issuedAt;
    }

    public Instant revokedAt() {
        return revokedAt;
    }

    public String revokeReason() {
        return revokeReason;
    }
}
