package com.tmc.gate.guard.application;

import com.tmc.gate.access.domain.ScanAttempt;
import com.tmc.gate.access.domain.ScanAttemptStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Đối soát lượt quét PENDING tồn đọng sau khi Gate Agent dừng đột ngột (ADR-03, T8). Lọc theo thời điểm khởi động
 * thay vì giữ danh sách trong bộ nhớ, để luôn phản ánh trạng thái thật trong SQLite (mục 6.4). Lượt PENDING phát sinh
 * sau khi khởi động (đang chờ cổng xoay) không được liệt kê (FR-03).
 */
@Service
public class PendingReconcileService {

    private static final Logger log = LoggerFactory.getLogger(PendingReconcileService.class);

    private final ScanAttemptStore scans;
    private final Clock clock;
    private volatile Instant startedAt;

    public PendingReconcileService(ScanAttemptStore scans, Clock clock) {
        this.scans = scans;
        this.clock = clock;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        startedAt = clock.instant();
        long leftOver = scans.findPending().stream().filter(a -> a.occurredAt().isBefore(startedAt)).count();
        if (leftOver > 0) {
            log.warn("{} lượt quét PENDING cần đối soát (xem GET /guard/pending)", leftOver);
        }
    }

    /** Lượt PENDING xảy ra trước lúc khởi động, sắp theo {@code occurredAt} tăng dần. */
    public List<ScanAttempt> listPending() {
        Instant cutoff = startedAt;
        if (cutoff == null) {
            return List.of(); // chưa sẵn sàng
        }
        return scans.findPending().stream()
                .filter(a -> a.occurredAt().isBefore(cutoff))
                .sorted(Comparator.comparing(ScanAttempt::occurredAt))
                .toList();
    }
}
