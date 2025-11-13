package com.crudzaso.crudcloud_backend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(EngineProperties.class)
public class DataSourceConfig {

    // Crea un mapa de JdbcTemplate por nombre de motor (mysql, postgres, sqlserver...).
    @Bean(name = "engineJdbcMap")
    public Map<String, JdbcTemplate> engineJdbcMap(EngineProperties engineProperties) {
        Map<String, JdbcTemplate> map = new HashMap<>();
        engineProperties.getDs().forEach((name, cfg) -> {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName(cfg.getDriverClassName());
            ds.setUrl(cfg.getJdbcUrl());
            ds.setUsername(cfg.getUsername());
            ds.setPassword(cfg.getPassword());
            map.put(name.toLowerCase(), new JdbcTemplate(ds));
        });
        return map;
    }
}
