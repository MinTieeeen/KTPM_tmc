package com.tmc.sim.turnstile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** Bộ giả lập hệ thống cổng xoay (đặc tả mục 9.1). Mỗi nhà máy một thể hiện. */
@SpringBootApplication
@ConfigurationPropertiesScan
public class TurnstileSimApplication {

    public static void main(String[] args) {
        SpringApplication.run(TurnstileSimApplication.class, args);
    }
}
