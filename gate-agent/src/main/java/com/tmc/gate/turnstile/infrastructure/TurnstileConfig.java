package com.tmc.gate.turnstile.infrastructure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Chọn adapter cổng xoay theo {@code tmc.gate.turnstile.adapter} (OCP: thêm nhà cung cấp mới = thêm lớp cài đặt
 * port + một giá trị cấu hình). Bean cài đặt cả {@code TurnstileGateway} lẫn {@code TurnstileHealthReader}.
 * <p>Adapter {@code http} ({@code HttpTurnstileAdapter}) được bổ sung ở Phase 4; trước đó chạy với profile {@code fake}.
 */
@Configuration(proxyBeanMethods = false)
class TurnstileConfig {

    @Bean
    @ConditionalOnProperty(prefix = "tmc.gate.turnstile", name = "adapter", havingValue = "fake")
    FakeTurnstileGateway fakeTurnstileGateway(TurnstileProperties props) {
        return new FakeTurnstileGateway(props.fakeBehavior(), props.fakeLatency());
    }
}
