package com.tmc.central;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Bean dùng chung cho mọi module của Central (không thuộc module nào). */
@Configuration(proxyBeanMethods = false)
class CentralConfig {

    /** Đồng hồ hệ thống; test thay bằng {@code Clock.fixed}. */
    @Bean
    @ConditionalOnMissingBean
    Clock clock() {
        return Clock.systemUTC();
    }
}
