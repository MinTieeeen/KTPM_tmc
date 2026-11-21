package com.tmc.gate.sync.domain;

import java.time.Instant;

/** Dọn nonce đã hết hạn (NFR-SEC-05). */
public interface NonceCleaner {

    /** Xóa nonce có {@code expires_at < now}; trả số dòng đã xóa. */
    int purgeExpired(Instant now);
}
