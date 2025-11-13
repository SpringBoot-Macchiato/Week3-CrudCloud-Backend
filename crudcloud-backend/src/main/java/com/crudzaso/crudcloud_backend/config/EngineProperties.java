package com.crudzaso.crudcloud_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

import com.crudzaso.crudcloud_backend.config.DataSourceProps;

@Data
@ConfigurationProperties(prefix = "engines")
public class EngineProperties {
    // Map de motores: engines.ds.<nombre>.jdbc-url, etc.
    private Map<String, DataSourceProps> ds = new HashMap<>();
}
