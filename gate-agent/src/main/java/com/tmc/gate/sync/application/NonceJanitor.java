package com.tmc.gate.sync.application;

import com.tmc.gate.sync.domain.NonceCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;

/** Dọn nonce đã hết hạn ({@code expires_at < now}) mỗi {@code nonce-cleanup-interval} (FR-07). */
@Component
public class NonceJanitor {

    private static final Logger log = LoggerFactory.getLogger(NonceJanitor.class);

    private final NonceCleaner cleaner;
    private final Clock clock;

    public NonceJanitor(NonceCleaner cleaner, Clock clock) {
        this.cleaner = cleaner;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${tmc.gate.sync.nonce-cleanup-interval:60s}")
    public void scheduledCleanup() {
        try {
            int removed = cleaner.purgeExpired(clock.instant());
            if (removed > 0) {
                log.debug("Đã dọn {} nonce hết hạn", removed);
            }
        } catch (RuntimeException e) {
            log.error("Lỗi khi dọn nonce", e);
        }
    }
}
