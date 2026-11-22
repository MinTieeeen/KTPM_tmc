package com.tmc.central.station.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmc.central.station.api.StationView;
import com.tmc.central.station.domain.ConnectivityPolicy;
import com.tmc.central.station.domain.GateAgent;
import com.tmc.central.station.domain.GateAgentRepository;
import com.tmc.central.station.domain.GateHeartbeat;
import com.tmc.central.station.domain.GateHeartbeatRepository;
import com.tmc.central.station.domain.GateStation;
import com.tmc.central.station.domain.GateStationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Ghép trạm ↔ gate (theo {@code factoryId}) ↔ heartbeat mới nhất, áp {@link ConnectivityPolicy} (FR-04, FR-05).
 * Các trạm cùng nhà máy dùng chung thông tin kết nối của gate nhà máy đó.
 */
@Service
public class ListStationsQuery {

    private static final Logger log = LoggerFactory.getLogger(ListStationsQuery.class);
    private static final TypeReference<Map<String, String>> STATUS_MAP = new TypeReference<>() {
    };

    private final GateStationRepository stations;
    private final GateAgentRepository gates;
    private final GateHeartbeatRepository heartbeats;
    private final ConnectivityPolicy policy;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public ListStationsQuery(GateStationRepository stations, GateAgentRepository gates,
                             GateHeartbeatRepository heartbeats, StationProperties props, ObjectMapper objectMapper,
                             Clock clock) {
        this.stations = stations;
        this.gates = gates;
        this.heartbeats = heartbeats;
        this.policy = new ConnectivityPolicy(props.heartbeatOfflineAfter());
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<StationView> list() {
        Instant now = clock.instant();
        Map<String, GateAgent> gateByFactory = gates.findAll().stream()
                .collect(Collectors.toMap(GateAgent::factoryId, Function.identity()));
        Map<String, GateHeartbeat> heartbeatByGate = heartbeats.findAll().stream()
                .collect(Collectors.toMap(GateHeartbeat::gateId, Function.identity()));

        return stations.findAll().stream()
                .sorted(Comparator.comparing(GateStation::stationId))
                .map(s -> {
                    GateAgent gate = gateByFactory.get(s.factoryId());
                    GateHeartbeat hb = gate == null ? null : heartbeatByGate.get(gate.gateId());
                    return new StationView(
                            s.stationId(),
                            s.factoryId(),
                            s.direction().name(),
                            s.enabled(),
                            gate == null ? null : gate.gateId(),
                            policy.isOnline(hb == null ? null : hb.receivedAt(), now),
                            hb == null ? null : hb.receivedAt(),
                            hb == null ? null : hb.outboxDepth(),
                            hb == null ? null : hb.lastSyncAt(),
                            hb == null ? null : turnstileStatusOf(hb, s.stationId()));
                })
                .toList();
    }

    /** JSON hỏng → để trống trường, không làm hỏng cả danh sách (mục 6.7). */
    private String turnstileStatusOf(GateHeartbeat hb, String stationId) {
        try {
            return objectMapper.readValue(hb.turnstileStatus(), STATUS_MAP).get(stationId);
        } catch (IOException e) {
            log.warn("turnstile_status của {} không đọc được", hb.gateId());
            return null;
        }
    }
}
