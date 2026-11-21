package com.tmc.central.station.infrastructure;

import com.tmc.central.station.domain.GateHeartbeat;
import com.tmc.central.station.domain.GateHeartbeatRepository;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data cài đặt port {@link GateHeartbeatRepository} (DIP). */
interface JpaGateHeartbeatRepository extends JpaRepository<GateHeartbeat, String>, GateHeartbeatRepository {
}
