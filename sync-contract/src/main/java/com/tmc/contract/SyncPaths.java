package com.tmc.contract;

/**
 * Đường dẫn REST của SyncAPI (C1) và tên header xác thực gate (SRS-SC-FR-11).
 */
public final class SyncPaths {

    public static final String BASE = "/sync";
    public static final String EVENTS = BASE + "/events";
    public static final String SNAPSHOT = BASE + "/snapshot";
    public static final String HEARTBEAT = BASE + "/heartbeat";

    /** Tham số query của {@link #SNAPSHOT}: version đã áp dụng tại gate, {@code 0} = lấy toàn bộ. */
    public static final String SNAPSHOT_SINCE_PARAM = "since";

    /** Header mang API key riêng của từng gate (thay cho mTLS, độ lệch 3.3). */
    public static final String GATE_KEY_HEADER = "X-Gate-Key";

    private SyncPaths() {
    }
}
