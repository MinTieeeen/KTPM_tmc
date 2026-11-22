package com.tmc.central.ingestion.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmc.central.ingestion.domain.EventValidator;
import com.tmc.central.ingestion.domain.GateContext;
import com.tmc.central.ingestion.domain.IngestedAttendance;
import com.tmc.central.ingestion.domain.IngestedScanAttempt;
import com.tmc.central.ingestion.domain.IngestionStore;
import com.tmc.central.ingestion.domain.SyncRejectedException;
import com.tmc.central.station.api.StationApi;
import com.tmc.central.station.api.StationInfo;
import com.tmc.contract.ContractVersion;
import com.tmc.contract.event.AttendancePayload;
import com.tmc.contract.event.BatchAck;
import com.tmc.contract.event.EventBatch;
import com.tmc.contract.event.EventEnvelope;
import com.tmc.contract.event.RejectedEvent;
import com.tmc.contract.event.ScanAttemptPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Tiếp nhận lô sự kiện từ gate (mục 6.4): một transaction cho cả lô; mỗi sự kiện được kiểm rồi chèn idempotent
 * và phân loại vào {@code accepted} / {@code duplicates} / {@code rejected} (INV-06, DP-03). Sự kiện hợp lệ vẫn được
 * lưu dù sự kiện khác trong lô bị từ chối. Lỗi CSDL bất ngờ → rollback cả lô; gate gửi lại an toàn.
 */
@Service
public class IngestEventBatchUseCase {

    private static final Logger log = LoggerFactory.getLogger(IngestEventBatchUseCase.class);

    private final IngestionStore store;
    private final StationApi stations;
    private final EventValidator validator = new EventValidator();
    private final ObjectMapper objectMapper;
    private final IngestionProperties props;
    private final Clock clock;

    public IngestEventBatchUseCase(IngestionStore store, StationApi stations, ObjectMapper objectMapper,
                                   IngestionProperties props, Clock clock) {
        this.store = store;
        this.stations = stations;
        this.objectMapper = objectMapper;
        this.props = props;
        this.clock = clock;
    }

    @Transactional
    public BatchAck ingest(GateContext gate, EventBatch batch) {
        if (!ContractVersion.isSupported(batch.contractVersion())) {
            throw new SyncRejectedException(SyncRejectedException.Reason.UNSUPPORTED_CONTRACT_VERSION,
                    "contractVersion không hỗ trợ: " + batch.contractVersion());
        }
        if (!gate.gateId().equals(batch.gateId())) {
            throw new SyncRejectedException(SyncRejectedException.Reason.GATE_MISMATCH,
                    "gateId trong lô khác gate đã xác thực");
        }
        int maxBatch = Math.min(props.sync().maxBatchSize(), ContractVersion.MAX_BATCH_SIZE);
        if (batch.events().size() > maxBatch) {
            throw new SyncRejectedException(SyncRejectedException.Reason.BATCH_TOO_LARGE,
                    "Lô có " + batch.events().size() + " sự kiện, tối đa " + maxBatch);
        }

        Instant receivedAt = clock.instant();
        Map<String, Optional<String>> stationFactoryCache = new HashMap<>();
        List<UUID> accepted = new ArrayList<>();
        List<UUID> duplicates = new ArrayList<>();
        List<RejectedEvent> rejected = new ArrayList<>();

        for (EventEnvelope e : batch.events()) {
            if (e.eventId() == null) {
                continue; // không có id thì không thể ack – gate sẽ không gửi được sự kiện như vậy
            }
            Outcome outcome = process(gate, e, receivedAt, stationFactoryCache);
            switch (outcome.kind) {
                case ACCEPTED -> accepted.add(e.eventId());
                case DUPLICATE -> duplicates.add(e.eventId());
                case REJECTED -> rejected.add(new RejectedEvent(e.eventId(), outcome.reason));
            }
        }

        log.info("Gate {}: nhận {} sự kiện – {} mới, {} trùng, {} từ chối", gate.gateId(), batch.events().size(),
                accepted.size(), duplicates.size(), rejected.size());
        rejected.forEach(r -> log.warn("Gate {}: từ chối sự kiện {} ({})", gate.gateId(), r.eventId(), r.reason()));
        return new BatchAck(accepted, duplicates, rejected);
    }

    private Outcome process(GateContext gate, EventEnvelope e, Instant receivedAt,
                            Map<String, Optional<String>> stationFactoryCache) {
        if (e.type() == null) {
            return Outcome.rejected(RejectedEvent.UNKNOWN_EVENT_TYPE);
        }
        return switch (e.type()) {
            case SCAN_ATTEMPT_RECORDED -> {
                ScanAttemptPayload p = parse(e.payload(), ScanAttemptPayload.class);
                Optional<String> factory = p == null ? Optional.empty() : stationFactory(p.stationId(), stationFactoryCache);
                Optional<String> error = validator.validateScan(gate, e.eventId(), p, factory);
                if (error.isPresent()) {
                    yield Outcome.rejected(error.get());
                }
                boolean inserted = store.insertScanIfAbsent(new IngestedScanAttempt(p.eventId(), gate.gateId(),
                        p.stationId(), p.credentialId(), p.result(), p.reason(), p.occurredAt(), receivedAt));
                yield inserted ? Outcome.ACCEPTED : Outcome.DUPLICATE;
            }
            case ATTENDANCE_RECORDED -> {
                AttendancePayload p = parse(e.payload(), AttendancePayload.class);
                Optional<String> factory = p == null ? Optional.empty() : stationFactory(p.stationId(), stationFactoryCache);
                Optional<String> error = validator.validateAttendance(gate, e.eventId(), p, factory);
                if (error.isPresent()) {
                    yield Outcome.rejected(error.get());
                }
                boolean inserted = store.insertAttendanceIfAbsent(new IngestedAttendance(p.eventId(), p.scanEventId(),
                        gate.gateId(), p.employeeCode(), p.stationId(), p.direction(), p.occurredAt(), receivedAt));
                yield inserted ? Outcome.ACCEPTED : Outcome.DUPLICATE;
            }
        };
    }

    private Optional<String> stationFactory(String stationId, Map<String, Optional<String>> cache) {
        if (stationId == null) {
            return Optional.empty();
        }
        return cache.computeIfAbsent(stationId, id -> stations.findStation(id).map(StationInfo::factoryId));
    }

    /** JSON không đọc được → {@code null} (→ INVALID_PAYLOAD). */
    private <T> T parse(JsonNode payload, Class<T> type) {
        if (payload == null || !payload.isObject()) {
            return null;
        }
        try {
            return objectMapper.treeToValue(payload, type);
        } catch (Exception ex) {
            return null;
        }
    }

    private enum Kind { ACCEPTED, DUPLICATE, REJECTED }

    private record Outcome(Kind kind, String reason) {
        static final Outcome ACCEPTED = new Outcome(Kind.ACCEPTED, null);
        static final Outcome DUPLICATE = new Outcome(Kind.DUPLICATE, null);

        static Outcome rejected(String reason) {
            return new Outcome(Kind.REJECTED, reason);
        }
    }
}
