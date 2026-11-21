package com.tmc.central.station.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Gate Agent đã đăng ký; mỗi nhà máy đúng một gate. */
@Entity
@Table(name = "gate_agents")
public class GateAgent {

    @Id
    @Column(name = "gate_id", length = 32)
    private String gateId;

    @Column(name = "factory_id", nullable = false, length = 8)
    private String factoryId;

    protected GateAgent() {
        // JPA
    }

    public String gateId() {
        return gateId;
    }

    public String factoryId() {
        return factoryId;
    }
}
