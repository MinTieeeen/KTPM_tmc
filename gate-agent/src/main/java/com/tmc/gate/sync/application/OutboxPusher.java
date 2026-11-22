package com.tmc.gate.sync.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmc.contract.ContractVersion;
import com.tmc.contract.event.BatchAck;
import com.tmc.contract.event.EventBatch;
import com.tmc.contract.event.EventEnvelope;
import com.tmc.contract.event.RejectedEvent;
import com.tmc.gate.access.domain.GateIdentity;
import com.tmc.gate.sync.domain.OutboxRecord;
import com.tmc.gate.sync.domain.OutboxStore;
import com.tmc.gate.sync.domain.SyncClient;
import com.tmc.gate.sync.domain.SyncStateStore;
import com.tmc.gate.sync.domain.SyncUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Đẩy outbox lên Central (ADR-03, FR-01 – FR-03): mỗi chu kỳ lấy ≤ {@code pushBatchSize} bản ghi PENDING theo
 * {@code seq}, gửi một {@link EventBatch}; lặp ngay nếu lô đầy (tối đa 10 lô/chu kỳ). Chỉ đánh dấu SYNCED các
 * {@code eventId} nằm trong {@code accepted} hoặc {@code duplicates}; {@code rejected} → REJECTED, không gửi lại;
 * {@code eventId} không có trong ack → giữ PENDING. Lỗi mạng → giữ nguyên, thử lại chu kỳ sau.
 */
@Component
public class OutboxPusher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPusher.class);
    private static final Duration AUTH_ERROR_LOG_EVERY = Duration.ofMinutes(1);

    private final OutboxStore outbox;
    private final SyncClient client;
    private final SyncStateStore state;
    private final GateIdentity identity;
    private final SyncProperties props;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private volatile Instant lastAuthErrorLog = Instant.EPOCH;

    public OutboxPusher(OutboxStore outbox, SyncClient client, SyncStateStore state, GateIdentity identity,
                        SyncProperties props, ObjectMapper objectMapper, Clock clock) {
        this.outbox = outbox;
        this.client = client;
        this.state = state;
        this.identity = identity;
        this.props = props;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Scheduled(initialDelayString = "${tmc.gate.sync.push-interval:2s}", fixedDelayString = "${tmc.gate.sync.push-interval:2s}")
    public void scheduledPush() {
        try {
            pushOnce();
        } catch (RuntimeException e) {
            log.error("Lỗi không mong đợi khi đẩy outbox", e);
        }
    }

    /** @return số sự kiện đã được Central xác nhận (accepted + duplicates) trong chu kỳ này */
    public int pushOnce() {
        int acknowledged = 0;
        for (int i = 0; i < SyncProperties.MAX_BATCHES_PER_CYCLE; i++) {
            List<OutboxRecord> records = outbox.fetchPending(props.pushBatchSize());
            if (records.isEmpty()) {
                break;
            }
            EventBatch batch = new EventBatch(ContractVersion.CURRENT, identity.gateId(), identity.factoryId(),
                    records.stream().map(this::toEnvelope).toList());

            BatchAck ack;
            try {
                ack = client.push(batch);
            } catch (SyncUnavailableException e) {
                logUnavailable(e, records.size());
                return acknowledged; // giữ nguyên outbox
            }

            Instant now = clock.instant();
            List<UUID> synced = new ArrayList<>(ack.accepted());
            synced.addAll(ack.duplicates());
            outbox.markSynced(synced, now);
            if (!ack.rejected().isEmpty()) {
                Map<UUID, String> reasons = new LinkedHashMap<>();
                for (RejectedEvent r : ack.rejected()) {
                    reasons.put(r.eventId(), r.reason());
                    log.warn("Central từ chối sự kiện {}: {}", r.eventId(), r.reason());
                }
                outbox.markRejected(reasons, now);
            }
            state.recordSuccessfulSync(now);
            acknowledged += synced.size();
            log.debug("Đã đẩy {} sự kiện: {} mới, {} trùng, {} bị từ chối", records.size(), ack.accepted().size(),
                    ack.duplicates().size(), ack.rejected().size());

            if (records.size() < props.pushBatchSize()) {
                break;
            }
        }
        return acknowledged;
    }

    private EventEnvelope toEnvelope(OutboxRecord r) {
        JsonNode payload;
        try {
            payload = objectMapper.readTree(r.payload());
        } catch (IOException e) {
            throw new IllegalStateException("Payload outbox hỏng (eventId=" + r.eventId() + ")", e);
        }
        Instant occurredAt = r.createdAt();
        JsonNode occurred = payload.path("occurredAt");
        if (occurred.isTextual()) {
            occurredAt = Instant.parse(occurred.asText());
        }
        return new EventEnvelope(r.eventId(), r.type(), occurredAt, payload);
    }

    private void logUnavailable(SyncUnavailableException e, int pending) {
        if (e.isAuthenticationFailure()) {
            Instant now = clock.instant();
            if (Duration.between(lastAuthErrorLog, now).compareTo(AUTH_ERROR_LOG_EVERY) >= 0) {
                lastAuthErrorLog = now;
                log.error("Central từ chối API key của gate (HTTP {}) – kiểm tra GATE_API_KEY", e.httpStatus());
            }
        } else if (e.isClientError()) {
            log.error("Central từ chối cả lô (HTTP {}) – giữ nguyên outbox: {}", e.httpStatus(), e.getMessage());
        } else {
            log.debug("Chưa đẩy được {} sự kiện: {}", pending, e.getMessage());
        }
    }
}
