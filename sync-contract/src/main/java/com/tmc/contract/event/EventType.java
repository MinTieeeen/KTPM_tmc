package com.tmc.contract.event;

/** Loại sự kiện gửi từ gate lên trung tâm (SRS-SC-FR-03). */
public enum EventType {
    /** Payload: {@link ScanAttemptPayload}. */
    SCAN_ATTEMPT_RECORDED,
    /** Payload: {@link AttendancePayload}. */
    ATTENDANCE_RECORDED
}
