package com.tmc.central.station.domain;

import java.util.List;
import java.util.Optional;

/** Kho Gate Agent (cài đặt: JpaGateAgentRepository). */
public interface GateAgentRepository {

    Optional<GateAgent> findById(String gateId);

    List<GateAgent> findAll();
}
