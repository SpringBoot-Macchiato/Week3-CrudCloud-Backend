package com.crudzaso.crudcloud_backend.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

@Configuration
public class EngineConnectionProvider {

    private final Map<String, JdbcTemplate> engineJdbcMap;

    public EngineConnectionProvider(@Qualifier("engineJdbcMap") Map<String, JdbcTemplate> engineJdbcMap) {
        this.engineJdbcMap = engineJdbcMap;
    }

    public JdbcTemplate getJdbcTemplate(Long engineId) {
        String name = switch (engineId.intValue()) {
            case 1 -> "mysql";
            case 2 -> "postgres";
            case 3 -> "sqlserver";
            default -> throw new IllegalArgumentException("Engine ID no soportado: " + engineId);
        };
        JdbcTemplate jt = engineJdbcMap.get(name);
        if (jt == null) throw new IllegalArgumentException("Engine no configurado: " + name);
        return jt;
    }
}
