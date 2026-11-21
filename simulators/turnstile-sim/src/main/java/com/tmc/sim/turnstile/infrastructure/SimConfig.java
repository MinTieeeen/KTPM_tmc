package com.tmc.sim.turnstile.infrastructure;

import com.tmc.sim.turnstile.application.SimProperties;
import com.tmc.sim.turnstile.domain.TurnstileRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.random.RandomGenerator;

/** Bean của bộ giả lập; test thay {@link Clock} và {@link RandomGenerator} để kết quả xác định (NFR-03). */
@Configuration(proxyBeanMethods = false)
class SimConfig {

    @Bean
    TurnstileRegistry turnstileRegistry(SimProperties props) {
        return new TurnstileRegistry(props.turnstileIds());
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    RandomGenerator randomGenerator() {
        return RandomGenerator.getDefault();
    }
}
