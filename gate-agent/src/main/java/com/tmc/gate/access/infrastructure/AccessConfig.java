package com.tmc.gate.access.infrastructure;

import com.tmc.gate.access.application.AccessSettings;
import com.tmc.gate.access.domain.AccessRules;
import com.tmc.gate.access.domain.GateIdentity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Khai báo bean của G1: đồng hồ, quy tắc, danh tính gate và tham số của use case. */
@Configuration(proxyBeanMethods = false)
class AccessConfig {

    /** Đồng hồ dùng chung; test thay bằng {@code Clock.fixed}. */
    @Bean
    @ConditionalOnMissingBean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    AccessRules accessRules() {
        return new AccessRules();
    }

    @Bean
    GateIdentity gateIdentity(AccessProperties props) {
        return new GateIdentity(props.gateId(), props.factoryId());
    }

    @Bean
    AccessSettings accessSettings(AccessProperties props) {
        return new AccessSettings(
                props.qr().clockSkew(),
                props.turnstile().openTimeout(),
                props.turnstile().statusTimeout(),
                props.access().stuckPendingAfter(),
                props.access().onlineWithin());
    }
}
