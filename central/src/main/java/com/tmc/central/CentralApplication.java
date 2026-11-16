package com.tmc.central;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Điểm khởi động của Central (Q2, modular monolith).
 * Mỗi package con trực tiếp là một application module của Spring Modulith. Phạm vi MVP:
 * ingestion (C1), employee (C2), station (C3), iam (C7 – đăng nhập JWT, vai trò EMPLOYEE/ADMIN).
 * C4, C5, C6, C8, C9 có trong báo cáo nhưng không hiện thực.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class CentralApplication {

    public static void main(String[] args) {
        SpringApplication.run(CentralApplication.class, args);
    }
}
