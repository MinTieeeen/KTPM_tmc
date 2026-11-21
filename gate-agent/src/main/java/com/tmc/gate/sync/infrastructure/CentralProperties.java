package com.tmc.gate.sync.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Kết nối tới Central dưới {@code tmc.gate.central.*}.
 *
 * @param apiKey giá trị {@code X-Gate-Key} riêng của gate (thay mTLS, D1) – không log, không commit giá trị thật
 */
@ConfigurationProperties("tmc.gate.central")
public record CentralProperties(
        @DefaultValue("http://localhost:8080") String url,
        @DefaultValue("change-me") String apiKey,
        @DefaultValue("2s") Duration connectTimeout,
        @DefaultValue("5s") Duration readTimeout) {

    /** Không để lộ API key trong log (NFR-04). */
    @Override
    public String toString() {
        return "CentralProperties[url=" + url + ", connectTimeout=" + connectTimeout + ", readTimeout=" + readTimeout + "]";
    }
}
