package com.tmc.central.station.infrastructure;

import com.tmc.central.station.domain.GateAgent;
import com.tmc.central.station.domain.GateAgentRepository;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data cài đặt port {@link GateAgentRepository} (DIP). */
interface JpaGateAgentRepository extends JpaRepository<GateAgent, String>, GateAgentRepository {
}
