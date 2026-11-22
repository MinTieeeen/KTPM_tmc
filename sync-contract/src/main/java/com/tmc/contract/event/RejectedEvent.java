package com.tmc.contract.event;

import java.util.UUID;

/**
 * Sự kiện C1 không nhận; G4 không gửi lại.
 *
 * @param reason {@code INVALID_PAYLOAD} | {@code UNKNOWN_EVENT_TYPE} | {@code STATION_NOT_IN_FACTORY} | {@code UNKNOWN_STATION}
 */
public record RejectedEvent(UUID eventId, String reason) {

    public static final String INVALID_PAYLOAD = "INVALID_PAYLOAD";
    public static final String UNKNOWN_EVENT_TYPE = "UNKNOWN_EVENT_TYPE";
    public static final String STATION_NOT_IN_FACTORY = "STATION_NOT_IN_FACTORY";
    public static final String UNKNOWN_STATION = "UNKNOWN_STATION";
}
