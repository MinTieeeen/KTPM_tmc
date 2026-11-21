package com.tmc.sim.turnstile.domain;

/** Chế độ hành vi của bộ giả lập (FR-03 – FR-05). */
public enum BehaviorMode {
    /** Mở sau độ trễ ngẫu nhiên; lỗi 500 với xác suất {@code failureRate}. */
    NORMAL,
    /** Không mở, chờ {@code timeoutDelay} rồi trả 504. */
    TIMEOUT,
    /** Không mở, trả 500 ngay. */
    ERROR,
    /** Mở, nhưng chờ {@code timeoutDelay} mới trả 200 – gate timeout trước rồi phải hỏi lại trạng thái lệnh. */
    EXECUTE_THEN_TIMEOUT
}
