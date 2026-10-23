package com.gota.rag.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class OperationalDatabaseConfig {

    @Bean
    @ConfigurationProperties("app.operational-datasource")
    public DataSourceProperties operationalDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("app.operational-datasource.hikari")
    public DataSource operationalDataSource(
            @Qualifier("operationalDataSourceProperties") DataSourceProperties properties
    ) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    public JdbcTemplate operationalJdbcTemplate(
            @Qualifier("operationalDataSource") DataSource dataSource
    ) {
        return new JdbcTemplate(dataSource);
    }
}
