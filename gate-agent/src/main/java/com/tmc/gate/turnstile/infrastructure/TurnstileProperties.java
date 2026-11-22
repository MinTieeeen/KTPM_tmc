package com.tmc.gate.turnstile.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.net.URI;
import java.time.Duration;

/**
 * Tham số G2 dưới {@code tmc.gate.turnstile.*} (mục 6.6).
 *
 * @param adapter       {@code http} | {@code fake}
 * @param openTimeout   truyền vào {@code open} bởi G1
 * @param fakeBehavior  chỉ cho Fake
 * @param fakeLatency   chỉ cho Fake
 */
@ConfigurationProperties("tmc.gate.turnstile")
public record TurnstileProperties(
        @DefaultValue("http") String adapter,
        @DefaultValue("http://localhost:9001") URI url,
        @DefaultValue("800ms") Duration openTimeout,
        @DefaultValue("200ms") Duration statusTimeout,
        @DefaultValue("300ms") Duration connectTimeout,
        @DefaultValue("OPEN") FakeBehavior fakeBehavior,
        @DefaultValue("300ms") Duration fakeLatency) {
}
