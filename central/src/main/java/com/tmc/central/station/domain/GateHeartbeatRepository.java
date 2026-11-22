package com.tmc.central.station.domain;

import java.util.List;
import java.util.Optional;

/** Kho heartbeat (cài đặt: JpaGateHeartbeatRepository). */
public interface GateHeartbeatRepository {

    Optional<GateHeartbeat> findById(String gateId);

    List<GateHeartbeat> findAll();

    GateHeartbeat save(GateHeartbeat heartbeat);
}
