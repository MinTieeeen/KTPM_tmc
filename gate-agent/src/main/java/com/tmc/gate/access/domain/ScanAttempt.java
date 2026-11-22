package com.tmc.gate.access.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Một lượt quét và vòng đời trạng thái (mục 6.6):
 * <pre>
 *   pending() ─► PENDING ─accept()──────► ACCEPTED
 *                   └────gateFailed()───► GATE_FAILED
 *   rejected() ─────────────────────────► REJECTED
 * </pre>
 * {@code accept}/{@code gateFailed} chỉ hợp lệ từ PENDING (DP-02).
 */
public final class ScanAttempt {

    private final UUID eventId;
    private final String stationId;
    private final UUID credentialId;
    private final Instant occurredAt;
    private ScanStatus status;
    private RejectReason reason;
    private Instant finalizedAt;

    private ScanAttempt(UUID eventId, String stationId, UUID credentialId, ScanStatus status,
                        RejectReason reason, Instant occurredAt, Instant finalizedAt) {
        this.eventId = Objects.requireNonNull(eventId, "eventId");
        this.stationId = Objects.requireNonNull(stationId, "stationId");
        this.credentialId = credentialId;
        this.status = Objects.requireNonNull(status, "status");
        this.reason = reason;
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
        this.finalizedAt = finalizedAt;
    }

    /** Lượt hợp lệ, ghi trước khi gọi cổng xoay (INV-05). */
    public static ScanAttempt pending(UUID eventId, String stationId, UUID credentialId, Instant occurredAt) {
        Objects.requireNonNull(credentialId, "credentialId");
        return new ScanAttempt(eventId, stationId, credentialId, ScanStatus.PENDING, null, occurredAt, null);
    }

    /**
     * Lượt bị từ chối theo quy tắc.
     *
     * @param credentialId {@code null} khi token không đọc được (INV-07)
     */
    public static ScanAttempt rejected(UUID eventId, String stationId, UUID credentialId,
                                       RejectReason reason, Instant occurredAt) {
        Objects.requireNonNull(reason, "reason");
        if (reason.isGateFailure()) {
            throw new IllegalArgumentException("Lỗi cổng xoay không phải lý do REJECTED: " + reason);
        }
        return new ScanAttempt(eventId, stationId, credentialId, ScanStatus.REJECTED, reason, occurredAt, occurredAt);
    }

    /** Dựng lại từ dữ liệu đã lưu (chỉ dùng cho adapter lưu trữ). */
    public static ScanAttempt restore(UUID eventId, String stationId, UUID credentialId, ScanStatus status,
                                      RejectReason reason, Instant occurredAt, Instant finalizedAt) {
        return new ScanAttempt(eventId, stationId, credentialId, status, reason, occurredAt, finalizedAt);
    }

    public void accept(Instant at) {
        requirePending();
        this.status = ScanStatus.ACCEPTED;
        this.reason = null;
        this.finalizedAt = Objects.requireNonNull(at, "at");
    }

    public void gateFailed(RejectReason gateReason, Instant at) {
        requirePending();
        Objects.requireNonNull(gateReason, "gateReason");
        if (!gateReason.isGateFailure()) {
            throw new IllegalArgumentException("Không phải lý do lỗi cổng: " + gateReason);
        }
        this.status = ScanStatus.GATE_FAILED;
        this.reason = gateReason;
        this.finalizedAt = Objects.requireNonNull(at, "at");
    }

    private void requirePending() {
        if (status != ScanStatus.PENDING) {
            throw new IllegalStateException("Lượt quét " + eventId + " không ở trạng thái PENDING: " + status);
        }
    }

    public UUID eventId() {
        return eventId;
    }

    public String stationId() {
        return stationId;
    }

    /** Có thể {@code null} với lượt REJECTED vì token không đọc được. */
    public UUID credentialId() {
        return credentialId;
    }

    public ScanStatus status() {
        return status;
    }

    /** {@code null} khi PENDING hoặc ACCEPTED. */
    public RejectReason reason() {
        return reason;
    }

    public Instant occurredAt() {
        return occurredAt;
    }

    /** {@code null} khi còn PENDING. */
    public Instant finalizedAt() {
        return finalizedAt;
    }

    @Override
    public String toString() {
        return "ScanAttempt[eventId=" + eventId + ", stationId=" + stationId + ", status=" + status
                + ", reason=" + reason + "]";
    }
}
