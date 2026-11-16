package com.tmc.gate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Điểm khởi động của Gate Agent (Q1). Mỗi nhà máy chạy một thể hiện.
 * Các package con ứng với component: access (G1), turnstile (G2), localstore (G3), sync (G4), guard (G5).
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class GateAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(GateAgentApplication.class, args);
    }
}
