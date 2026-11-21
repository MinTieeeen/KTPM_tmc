package com.tmc.central.ingestion.api;

import com.tmc.central.iam.api.GatePrincipal;
import com.tmc.central.ingestion.application.GetSnapshotDeltaUseCase;
import com.tmc.central.ingestion.application.IngestEventBatchUseCase;
import com.tmc.central.ingestion.application.RecordHeartbeatUseCase;
import com.tmc.central.ingestion.domain.GateContext;
import com.tmc.central.ingestion.domain.SyncRejectedException;
import com.tmc.central.station.api.StationApi;
import com.tmc.contract.SyncPaths;
import com.tmc.contract.event.BatchAck;
import com.tmc.contract.event.EventBatch;
import com.tmc.contract.heartbeat.Heartbeat;
import com.tmc.contract.snapshot.SnapshotDelta;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * SyncAPI cho Gate Agent (mục 5.1): xác thực bằng {@code X-Gate-Key} ở chuỗi bộ lọc {@code /sync/**} của C7;
 * principal là {@link GatePrincipal}. Nhà máy của gate tra ở C3, không lấy từ body (DP-11).
 */
@RestController
public class SyncController {

    private final IngestEventBatchUseCase ingest;
    private final GetSnapshotDeltaUseCase snapshot;
    private final RecordHeartbeatUseCase heartbeat;
    private final StationApi stations;

    public SyncController(IngestEventBatchUseCase ingest, GetSnapshotDeltaUseCase snapshot,
                          RecordHeartbeatUseCase heartbeat, StationApi stations) {
        this.ingest = ingest;
        this.snapshot = snapshot;
        this.heartbeat = heartbeat;
        this.stations = stations;
    }

    @PostMapping(SyncPaths.EVENTS)
    public BatchAck events(@AuthenticationPrincipal GatePrincipal gate, @RequestBody EventBatch batch) {
        return ingest.ingest(context(gate), batch);
    }

    @GetMapping(SyncPaths.SNAPSHOT)
    public SnapshotDelta snapshot(@AuthenticationPrincipal GatePrincipal gate,
                                  @RequestParam(name = SyncPaths.SNAPSHOT_SINCE_PARAM, defaultValue = "0") long since) {
        context(gate); // gate phải được đăng ký ở C3
        return snapshot.deltaSince(since);
    }

    @PostMapping(SyncPaths.HEARTBEAT)
    public ResponseEntity<Void> heartbeat(@AuthenticationPrincipal GatePrincipal gate,
                                          @RequestBody Heartbeat body) {
        heartbeat.record(context(gate), body);
        return ResponseEntity.noContent().build();
    }

    private GateContext context(GatePrincipal gate) {
        return stations.findGate(gate.gateId())
                .map(g -> new GateContext(g.gateId(), g.factoryId()))
                .orElseThrow(() -> new SyncRejectedException(SyncRejectedException.Reason.UNKNOWN_GATE,
                        "Gate chưa đăng ký: " + gate.gateId()));
    }
}
