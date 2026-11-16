package com.tmc.central;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Điểm khởi động của Central (Q2, modular monolith).
 * Mỗi package con trực tiếp là một application module của Spring Modulith, ứng với C1–C9:
 * ingestion, employee, station, period, hrpayroll, reporting, iam, audit, config.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class CentralApplication {

    public static void main(String[] args) {
        SpringApplication.run(CentralApplication.class, args);
    }
}
