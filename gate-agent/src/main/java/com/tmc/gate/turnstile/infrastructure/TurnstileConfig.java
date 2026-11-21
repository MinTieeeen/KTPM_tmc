package com.tmc.gate.turnstile.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Chọn adapter cổng xoay theo {@code tmc.gate.turnstile.adapter} (FR-07, OCP: thêm nhà cung cấp mới = thêm lớp cài
 * đặt port + một giá trị cấu hình). Bean cài đặt cả {@code TurnstileGateway} lẫn {@code TurnstileHealthReader}.
 */
@Configuration(proxyBeanMethods = false)
class TurnstileConfig {

    /** Mặc định ({@code adapter=http} hoặc không khai báo). */
    @Bean
    @ConditionalOnProperty(prefix = "tmc.gate.turnstile", name = "adapter", havingValue = "http", matchIfMissing = true)
    HttpTurnstileAdapter httpTurnstileAdapter(TurnstileProperties props, CircuitBreakerRegistry breakers,
                                              ObjectMapper objectMapper) {
        return new HttpTurnstileAdapter(props, breakers, objectMapper);
    }

    /** Profile {@code fake}: không cần {@code turnstile-sim}. */
    @Bean
    @ConditionalOnProperty(prefix = "tmc.gate.turnstile", name = "adapter", havingValue = "fake")
    FakeTurnstileGateway fakeTurnstileGateway(TurnstileProperties props) {
        return new FakeTurnstileGateway(props.fakeBehavior(), props.fakeLatency());
    }
}
