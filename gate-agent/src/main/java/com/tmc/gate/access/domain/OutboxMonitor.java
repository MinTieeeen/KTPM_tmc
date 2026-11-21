package com.tmc.gate.access.domain;

/** Số bản ghi outbox còn chờ đẩy lên Central (cho {@code GET /status}). */
public interface OutboxMonitor {

    long pendingCount();
}
