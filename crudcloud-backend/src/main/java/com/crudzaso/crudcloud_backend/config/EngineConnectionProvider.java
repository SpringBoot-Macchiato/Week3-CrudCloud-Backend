package com.crudzaso.crudcloud_backend.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class EngineConnectionProvider {

    private final JdbcTemplate mysqlAdminJdbc;
    // private final JdbcTemplate pgAdminJdbc;  // si agregas Postgres luego

    public EngineConnectionProvider(
            @Qualifier("mysqlAdminJdbc") JdbcTemplate mysqlAdminJdbc
            // , @Qualifier("pgAdminJdbc") JdbcTemplate pgAdminJdbc
    ) {
        this.mysqlAdminJdbc = mysqlAdminJdbc;
        // this.pgAdminJdbc = pgAdminJdbc;
    }

    /**
     * Retorna el JdbcTemplate del motor correspondiente al engineId.
     * Ejemplo: 1=MySQL, 2=PostgreSQL, etc.
     */
    public JdbcTemplate getJdbcTemplate(Long engineId) {
        if (engineId == 1L) {
            return mysqlAdminJdbc;
        }
        // else if (engineId == 2L) return pgAdminJdbc;
        throw new IllegalArgumentException("Engine ID no soportado: " + engineId);
    }

    /**
     * (Opcional) mapa para usar por nombre de motor
     */
    @Bean
    public Map<Long, JdbcTemplate> engineJdbcMap() {
        Map<Long, JdbcTemplate> map = new HashMap<>();
        map.put(1L, mysqlAdminJdbc);
        // map.put(2L, pgAdminJdbc);
        return map;
    }
}
