package com.tmc.gate.access.domain;

/** Kết quả hỏi lại cổng xoay xem một lệnh mở đã được thực hiện chưa (NFR-REL-02). */
public enum CommandStatus {
    EXECUTED,
    NOT_EXECUTED,
    /** Không xác định được (lỗi mạng, timeout) – coi như chưa mở. */
    UNKNOWN
}
