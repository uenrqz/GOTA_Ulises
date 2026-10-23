package com.gota.rag.application;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

    public List<String> findActiveClientNames() {
        return jdbcTemplate.queryForList(
                "SELECT nombre FROM Clientes WHERE activo = 1 ORDER BY nombre",
                String.class
        );
    }

    public List<CurrentTariff> findCurrentTariffs() {
        return jdbcTemplate.query(
                """
                SELECT ts.nombre AS service_type, t.volumen_incluido_m3,
                       t.tarifa_base, t.tarifa_exceso,
                       t.vigente_desde, t.vigente_hasta
                FROM Tarifas t
                JOIN Tipos_Servicio ts ON ts.id = t.tipo_servicio_id
                WHERE t.activo = 1
                  AND t.vigente_desde <= CURRENT_DATE
                  AND (t.vigente_hasta IS NULL OR t.vigente_hasta >= CURRENT_DATE)
                ORDER BY ts.nombre, t.volumen_incluido_m3
                """,
                (resultSet, rowNum) -> new CurrentTariff(
                        resultSet.getString("service_type"),
                        resultSet.getBigDecimal("volumen_incluido_m3"),
                        resultSet.getBigDecimal("tarifa_base"),
                        resultSet.getBigDecimal("tarifa_exceso"),
                        resultSet.getObject("vigente_desde", LocalDate.class),
                        resultSet.getObject("vigente_hasta", LocalDate.class)
                )
        );
    }

    public record CurrentTariff(
            String serviceType,
            BigDecimal includedVolumeM3,
            BigDecimal baseRate,
            BigDecimal excessRate,
            LocalDate validFrom,
            LocalDate validUntil
    ) {
    }
}
