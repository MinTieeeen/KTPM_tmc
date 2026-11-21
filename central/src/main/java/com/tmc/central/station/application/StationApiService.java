package com.tmc.central.station.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmc.central.station.api.GateInfo;
import com.tmc.central.station.api.HeartbeatRecord;
import com.tmc.central.station.api.StationApi;
import com.tmc.central.station.api.StationInfo;
import com.tmc.central.station.api.UnknownGateException;
import com.tmc.central.station.domain.GateAgentRepository;
import com.tmc.central.station.domain.GateHeartbeat;
import com.tmc.central.station.domain.GateHeartbeatRepository;
import com.tmc.central.station.domain.GateStation;
import com.tmc.central.station.domain.GateStationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Cài đặt {@link StationApi} cho C1. */
@Service
public class StationApiService implements StationApi {

    private final GateStationRepository stations;
    private final GateAgentRepository gates;
    private final GateHeartbeatRepository heartbeats;
    private final ObjectMapper objectMapper;

    public StationApiService(GateStationRepository stations, GateAgentRepository gates,
                             GateHeartbeatRepository heartbeats, ObjectMapper objectMapper) {
        this.stations = stations;
        this.gates = gates;
        this.heartbeats = heartbeats;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StationInfo> findStation(String stationId) {
        return stations.findById(stationId).map(StationApiService::toInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GateInfo> findGate(String gateId) {
        return gates.findById(gateId).map(g -> new GateInfo(g.gateId(), g.factoryId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StationInfo> allStations() {
        return stations.findAll().stream()
                .sorted(Comparator.comparing(GateStation::stationId))
                .map(StationApiService::toInfo)
                .toList();
    }

    @Override
    @Transactional
    public void recordHeartbeat(HeartbeatRecord r) {
        gates.findById(r.gateId()).orElseThrow(() -> new UnknownGateException(r.gateId()));
        GateHeartbeat heartbeat = heartbeats.findById(r.gateId()).orElseGet(() -> new GateHeartbeat(r.gateId()));
        if (!heartbeat.isOlderThan(r.sentAt())) {
            return; // heartbeat đến trễ – bỏ qua
        }
        heartbeat.update(r.sentAt(), r.receivedAt(), r.outboxDepth(), r.lastSyncAt(), toJson(r));
        heartbeats.save(heartbeat);
    }

    private String toJson(HeartbeatRecord r) {
        try {
            return objectMapper.writeValueAsString(r.turnstileStatus());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("turnstileStatus không tuần tự hóa được", e);
        }
    }

    private static StationInfo toInfo(GateStation s) {
        return new StationInfo(s.stationId(), s.factoryId(), s.direction().name(), s.enabled(), s.turnstileId());
    }
}
