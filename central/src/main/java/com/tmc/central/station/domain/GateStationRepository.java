package com.tmc.central.station.domain;

import java.util.List;
import java.util.Optional;

/** Kho trạm (cài đặt: JpaGateStationRepository). */
public interface GateStationRepository {

    Optional<GateStation> findById(String stationId);

    List<GateStation> findAll();
}
