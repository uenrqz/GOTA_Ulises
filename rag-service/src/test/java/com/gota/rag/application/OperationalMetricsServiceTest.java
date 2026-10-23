package com.gota.rag.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationalMetricsServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private OperationalMetricsService service;

    @Test
    void cuentaClientesActivosConConsultaControlada() {
        when(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Clientes WHERE activo = 1",
                Integer.class
        )).thenReturn(1);

        assertThat(service.countActiveClients()).isEqualTo(1);
        verify(jdbcTemplate).queryForObject(
                "SELECT COUNT(*) FROM Clientes WHERE activo = 1",
                Integer.class
        );
    }

    @Test
    void obtieneNombresOrdenadosDeClientesActivos() {
        when(jdbcTemplate.queryForList(
                "SELECT nombre FROM Clientes WHERE activo = 1 ORDER BY nombre",
                String.class
        )).thenReturn(List.of("Juan Lopez"));

        assertThat(service.findActiveClientNames()).containsExactly("Juan Lopez");
        verify(jdbcTemplate).queryForList(
                "SELECT nombre FROM Clientes WHERE activo = 1 ORDER BY nombre",
                String.class
        );
    }

    @Test
    void consultaTarifasConVigenciaActual() {
        when(jdbcTemplate.query(
                org.mockito.ArgumentMatchers.contains("WHERE t.activo = 1"),
                org.mockito.ArgumentMatchers.any(org.springframework.jdbc.core.RowMapper.class)
        )).thenReturn(List.of());

        assertThat(service.findCurrentTariffs()).isEmpty();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(
                sqlCaptor.capture(),
                org.mockito.ArgumentMatchers.any(org.springframework.jdbc.core.RowMapper.class)
        );
        assertThat(sqlCaptor.getValue())
                .contains("t.vigente_desde <= CURRENT_DATE")
                .contains("t.vigente_hasta IS NULL");
    }
}
