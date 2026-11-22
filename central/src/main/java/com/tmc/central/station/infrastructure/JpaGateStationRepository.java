package com.tmc.central.station.infrastructure;

import com.tmc.central.station.domain.GateStation;
import com.tmc.central.station.domain.GateStationRepository;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data cài đặt port {@link GateStationRepository} (DIP). */
interface JpaGateStationRepository extends JpaRepository<GateStation, String>, GateStationRepository {
}
