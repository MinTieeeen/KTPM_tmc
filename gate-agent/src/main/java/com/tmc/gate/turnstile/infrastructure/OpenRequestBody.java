package com.tmc.gate.turnstile.infrastructure;

import java.util.UUID;

/** Body của {@code POST /turnstiles/{id}/open}. */
record OpenRequestBody(UUID requestId) {
}
