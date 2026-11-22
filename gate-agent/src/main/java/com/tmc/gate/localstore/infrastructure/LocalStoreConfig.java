package com.tmc.gate.localstore.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Hạ tầng JDBC của G3: {@link JdbcClient} và {@link DataSourceTransactionManager} trên DataSource SQLite
 * (một kết nối, WAL + synchronous=FULL – cấu hình trong application.yml). Không dùng JPA.
 */
@Configuration(proxyBeanMethods = false)
@EnableTransactionManagement
class LocalStoreConfig {

    @Bean
    JdbcClient jdbcClient(DataSource dataSource) {
        return JdbcClient.create(dataSource);
    }

    @Bean
    PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
