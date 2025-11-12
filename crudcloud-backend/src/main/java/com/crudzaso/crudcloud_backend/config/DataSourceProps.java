package com.crudzaso.crudcloud_backend.config;

import lombok.Data;

@Data
public class DataSourceProps {
    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName;
}

