package com.tmc.gate.access.domain;

import java.util.UUID;

/** Chốt lượt quét hợp lệ thành giao dịch chấm công. */
public interface TransactionStore {

    /**
     * Một transaction: scan_attempt PENDING → ACCEPTED, thêm attendance_transaction, cập nhật presence,
     * thêm 2 bản ghi outbox. Hoặc đủ cả, hoặc không gì (INV-05).
     *
     * @throws IllegalStateException lượt không còn PENDING
     */
    void confirmAccepted(UUID scanEventId, AttendanceTransaction tx);
}
