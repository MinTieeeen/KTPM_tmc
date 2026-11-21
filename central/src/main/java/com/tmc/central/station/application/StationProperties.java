package com.tmc.central.station.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/** Tham số C3 dưới {@code tmc.central.station.*} (NFR-03). */
@ConfigurationProperties("tmc.central.station")
public record StationProperties(@DefaultValue("30s") Duration heartbeatOfflineAfter) {
}
