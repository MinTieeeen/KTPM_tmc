package com.tmc.central.station.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Trạm cổng: một cổng xoay, một chiều cố định, thuộc một nhà máy. Dữ liệu seed (FR-01). */
@Entity
@Table(name = "gate_stations")
public class GateStation {

    @Id
    @Column(name = "station_id", length = 32)
    private String stationId;

    @Column(name = "factory_id", nullable = false, length = 8)
    private String factoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 3)
    private StationDirection direction;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "turnstile_id", nullable = false, length = 32)
    private String turnstileId;

    protected GateStation() {
        // JPA
    }

    public String stationId() {
        return stationId;
    }

    public String factoryId() {
        return factoryId;
    }

    public StationDirection direction() {
        return direction;
    }

    public boolean enabled() {
        return enabled;
    }

    public String turnstileId() {
        return turnstileId;
    }
}
