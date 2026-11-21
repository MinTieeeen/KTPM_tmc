package com.tmc.central.ingestion.domain;

/** Lưu sự kiện từ gate, idempotent theo {@code eventId} (INV-06). Chỉ INSERT (DP-02). */
public interface IngestionStore {

    /** @return {@code true} nếu chèn mới, {@code false} nếu {@code eventId} đã có (trùng) */
    boolean insertScanIfAbsent(IngestedScanAttempt attempt);

    /** @return {@code true} nếu chèn mới, {@code false} nếu {@code eventId} hoặc {@code scanEventId} đã có */
    boolean insertAttendanceIfAbsent(IngestedAttendance attendance);
}
