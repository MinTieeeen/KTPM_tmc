package com.tmc.sim.turnstile.application;

import com.tmc.sim.turnstile.domain.BehaviorMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.List;

/** Tham số dưới {@code tmc.sim.*} (mục 6.6): cổng có sẵn, hành vi ban đầu, thời gian chờ giả lập timeout. */
@ConfigurationProperties("tmc.sim")
public record SimProperties(
        @DefaultValue List<String> turnstileIds,
        @DefaultValue("300ms") Duration latencyMin,
        @DefaultValue("500ms") Duration latencyMax,
        @DefaultValue("0.0") double failureRate,
        @DefaultValue("NORMAL") BehaviorMode mode,
        @DefaultValue("5s") Duration timeoutDelay) {
}
