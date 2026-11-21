package com.tmc.central.employee.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

/** Khóa công khai Ed25519 ký QR đã công bố. Khóa bí mật không bao giờ được lưu vào CSDL (DP-11). */
@Entity
@Table(name = "signing_keys")
public class SigningKey {

    @Id
    @Column(name = "kid", length = 32)
    private String kid;

    /** X.509 SubjectPublicKeyInfo. */
    @Column(name = "public_key", nullable = false)
    private byte[] publicKey;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected SigningKey() {
        // JPA
    }

    private SigningKey(String kid, byte[] publicKey, Instant createdAt) {
        this.kid = kid;
        this.publicKey = publicKey.clone();
        this.active = true;
        this.createdAt = createdAt;
    }

    public static SigningKey publish(String kid, byte[] publicKeyX509, Instant now) {
        Objects.requireNonNull(kid, "kid");
        Objects.requireNonNull(publicKeyX509, "publicKeyX509");
        return new SigningKey(kid, publicKeyX509, now);
    }

    /** Khóa đang lưu khác với khóa đang dùng hoặc chưa hoạt động → cần cập nhật và công bố lại. */
    public boolean differsFrom(byte[] publicKeyX509) {
        return !active || !Arrays.equals(publicKey, publicKeyX509);
    }

    /** Thay khóa cùng {@code kid} (ví dụ profile demo tự sinh khóa mới sau khi khởi động lại). */
    public void replace(byte[] publicKeyX509, Instant now) {
        this.publicKey = publicKeyX509.clone();
        this.active = true;
        this.createdAt = now;
    }

    public void deactivate() {
        this.active = false;
    }

    public String kid() {
        return kid;
    }

    public byte[] publicKey() {
        return publicKey.clone();
    }

    public boolean active() {
        return active;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
