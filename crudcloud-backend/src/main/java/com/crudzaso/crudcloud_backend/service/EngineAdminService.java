package com.crudzaso.crudcloud_backend.service;

import com.crudzaso.crudcloud_backend.config.EngineConnectionProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Gestiona los JdbcTemplate de los distintos motores de base de datos.
 * Delegando la obtención al EngineConnectionProvider.
 */
@Service
public class EngineAdminService {

    private final EngineConnectionProvider connectionProvider;

    public EngineAdminService(EngineConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    /**
     * Retorna el JdbcTemplate correspondiente al engineId especificado.
     *
     * @param engineId ID del motor (1=MySQL, 2=PostgreSQL, etc.)
     * @return JdbcTemplate conectado al motor correspondiente
     */
    public JdbcTemplate getJdbcForEngine(Long engineId) {
        JdbcTemplate jdbc = connectionProvider.getJdbcTemplate(engineId);
        if (jdbc == null) {
            throw new IllegalArgumentException("Engine not configure: " + engineId);
        }
        return jdbc;
    }
}
