package com.tmc.gate.turnstile.infrastructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

/** Phản hồi của {@code GET /turnstiles/{id}/commands/{requestId}}. */
@JsonIgnoreProperties(ignoreUnknown = true)
record CommandStatusBody(boolean executed, Instant at) {
}
