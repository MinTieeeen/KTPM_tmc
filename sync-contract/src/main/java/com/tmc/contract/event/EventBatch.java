package com.tmc.contract.event;

import java.util.List;

/**
 * Lô sự kiện G4 → C1, tối đa {@code ContractVersion.MAX_BATCH_SIZE} phần tử (SRS-SC-FR-01).
 * Record không tự kiểm tra hợp lệ; C1 kiểm phiên bản, kích thước lô và gate (mục 6.7).
 */
public record EventBatch(
        int contractVersion,
        String gateId,
        String factoryId,
        List<EventEnvelope> events) {

    public EventBatch {
        events = events == null ? List.of() : List.copyOf(events);
    }
}
