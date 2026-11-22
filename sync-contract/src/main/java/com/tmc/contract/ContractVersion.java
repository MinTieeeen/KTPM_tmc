package com.tmc.contract;

/**
 * Phiên bản hợp đồng đồng bộ Q1 ↔ Q2 và các giới hạn gắn với định dạng (SRS-SC-FR-01).
 */
public final class ContractVersion {

    /** Phiên bản hợp đồng hiện tại; tăng khi thay đổi không tương thích. */
    public static final int CURRENT = 1;

    /** Số sự kiện tối đa trong một {@code EventBatch}. */
    public static final int MAX_BATCH_SIZE = 200;

    private ContractVersion() {
    }

    public static boolean isSupported(int version) {
        return version == CURRENT;
    }
}
