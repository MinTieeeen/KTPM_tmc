package com.tmc.contract.event;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

/**
 * Vỏ của một sự kiện (SRS-SC-FR-02). {@code payload} để dạng {@link JsonNode} để C1 phân loại theo
 * {@code type} và đưa sự kiện lạ vào {@code rejected} thay vì làm hỏng cả lô (quyết định D2).
 * Quy ước: {@code payload.eventId} phải bằng {@code eventId}.
 */
public record EventEnvelope(
        UUID eventId,
        EventType type,
        Instant occurredAt,
        JsonNode payload) {
}
