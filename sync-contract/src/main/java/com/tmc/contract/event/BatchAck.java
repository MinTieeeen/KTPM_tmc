package com.tmc.contract.event;

import java.util.List;
import java.util.UUID;

/**
 * Kết quả xử lý lô: mỗi {@code eventId} nằm trong đúng một danh sách (SRS-SC-FR-04, INV-06).
 * G4 đánh dấu SYNCED các id thuộc {@code accepted} hoặc {@code duplicates}.
 */
public record BatchAck(
        List<UUID> accepted,
        List<UUID> duplicates,
        List<RejectedEvent> rejected) {

    public BatchAck {
        accepted = accepted == null ? List.of() : List.copyOf(accepted);
        duplicates = duplicates == null ? List.of() : List.copyOf(duplicates);
        rejected = rejected == null ? List.of() : List.copyOf(rejected);
    }
}
