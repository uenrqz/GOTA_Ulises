package com.gota.rag.application;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class OperationalMetricsService {

    private final JdbcTemplate jdbcTemplate;

    public OperationalMetricsService(
            @Qualifier("operationalJdbcTemplate") JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int countActiveClients() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Clientes WHERE activo = 1",
                Integer.class
        );
        return count == null ? 0 : count;
    }
}
