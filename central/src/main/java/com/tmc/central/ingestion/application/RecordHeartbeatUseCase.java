package com.tmc.central.ingestion.application;

import com.tmc.central.ingestion.domain.GateContext;
import com.tmc.central.ingestion.domain.SyncRejectedException;
import com.tmc.central.station.api.HeartbeatRecord;
import com.tmc.central.station.api.StationApi;
import com.tmc.contract.heartbeat.Heartbeat;
import org.springframework.stereotype.Service;

import java.time.Clock;

/** {@code POST /sync/heartbeat} → C3 (FR-09): thêm {@code receivedAt} rồi gọi {@link StationApi#recordHeartbeat}. */
@Service
public class RecordHeartbeatUseCase {

    private final StationApi stations;
    private final Clock clock;

    public RecordHeartbeatUseCase(StationApi stations, Clock clock) {
        this.stations = stations;
        this.clock = clock;
    }

    public void record(GateContext gate, Heartbeat heartbeat) {
        if (!gate.gateId().equals(heartbeat.gateId())) {
            throw new SyncRejectedException(SyncRejectedException.Reason.GATE_MISMATCH,
                    "gateId trong heartbeat khác gate đã xác thực");
        }
        if (heartbeat.sentAt() == null) {
            throw new IllegalArgumentException("Heartbeat thiếu sentAt");
        }
        stations.recordHeartbeat(new HeartbeatRecord(gate.gateId(), heartbeat.sentAt(), clock.instant(),
                heartbeat.outboxDepth(), heartbeat.lastSyncAt(), heartbeat.turnstileStatus()));
    }
}
