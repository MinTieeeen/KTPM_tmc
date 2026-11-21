package com.tmc.sim.turnstile.domain;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Một cổng xoay giả lập: đếm số lần mở và nhớ lệnh nào đã thực hiện. {@link #execute} đồng bộ để cùng
 * {@code requestId} gửi đồng thời chỉ mở một lần (NFR-02).
 */
public final class Turnstile {

    private final String id;
    private final Map<UUID, Instant> executed = new HashMap<>();
    private long openCount;

    public Turnstile(String id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    /** Mở cổng cho {@code requestId}; trả {@code true} nếu là lần đầu (đã mở thật), {@code false} nếu trùng. */
    public synchronized boolean execute(UUID requestId, Instant at) {
        if (executed.containsKey(requestId)) {
            return false;
        }
        executed.put(requestId, at);
        openCount++;
        return true;
    }

    public synchronized Optional<Instant> executedAt(UUID requestId) {
        return Optional.ofNullable(executed.get(requestId));
    }

    public synchronized long openCount() {
        return openCount;
    }

    public String id() {
        return id;
    }
}
