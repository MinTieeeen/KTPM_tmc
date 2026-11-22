package com.tmc.central.station.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

/**
 * Heartbeat mới nhất của một gate – dữ liệu dựng lại được, chỉ giữ một bản mỗi gate.
 * {@code turnstileStatus} là JSON {@code {stationId: UP|DOWN|CIRCUIT_OPEN}}.
 */
@Entity
@Table(name = "gate_heartbeats")
public class GateHeartbeat {

    @Id
    @Column(name = "gate_id", length = 32)
    private String gateId;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "outbox_depth", nullable = false)
    private long outboxDepth;

    @Column(name = "last_sync_at")
    private Instant lastSyncAt;

    @Column(name = "turnstile_status", nullable = false, columnDefinition = "text")
    private String turnstileStatus;

    protected GateHeartbeat() {
        // JPA
    }

    public GateHeartbeat(String gateId) {
        this.gateId = Objects.requireNonNull(gateId, "gateId");
    }

    /** Heartbeat {@code sentAt} có mới hơn bản đang lưu không (bỏ qua heartbeat đến trễ). */
    public boolean isOlderThan(Instant candidateSentAt) {
        return sentAt == null || candidateSentAt.isAfter(sentAt);
    }

    public void update(Instant sentAt, Instant receivedAt, long outboxDepth, Instant lastSyncAt,
                       String turnstileStatusJson) {
        this.sentAt = Objects.requireNonNull(sentAt, "sentAt");
        this.receivedAt = Objects.requireNonNull(receivedAt, "receivedAt");
        this.outboxDepth = outboxDepth;
        this.lastSyncAt = lastSyncAt;
        this.turnstileStatus = Objects.requireNonNull(turnstileStatusJson, "turnstileStatusJson");
    }

    public String gateId() {
        return gateId;
    }

    public Instant sentAt() {
        return sentAt;
    }

    public Instant receivedAt() {
        return receivedAt;
    }

    public long outboxDepth() {
        return outboxDepth;
    }

    public Instant lastSyncAt() {
        return lastSyncAt;
    }

    public String turnstileStatus() {
        return turnstileStatus;
    }
}
