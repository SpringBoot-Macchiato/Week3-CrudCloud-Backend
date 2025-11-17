package com.crudzaso.crudcloud_backend.service.impl;

import com.crudzaso.crudcloud_backend.service.DatabaseAdminService;
import com.crudzaso.crudcloud_backend.service.EngineAdminService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class PostgresDatabaseAdminService implements DatabaseAdminService {

    private final EngineAdminService engineAdminService;

    public PostgresDatabaseAdminService(EngineAdminService engineAdminService) {
        this.engineAdminService = engineAdminService;
    }

    @Override
    public void createDatabaseAndUser(String engine, String dbName, String dbUser, String dbPassword) throws Exception {
        JdbcTemplate jdbc = engineAdminService.getJdbcForEngine(2L); // 2 = PostgreSQL

        // IMPORTANTE: CREATE DATABASE no puede ejecutarse dentro de una transacción en PostgreSQL
        int dbExists = jdbc.queryForObject(
                "SELECT COUNT(1) FROM pg_database WHERE datname = ?",
                Integer.class,
                dbName
        );
        if (dbExists == 0) {
            jdbc.execute("CREATE DATABASE " + dbName + ";");
        }

        int roleExists = jdbc.queryForObject(
                "SELECT COUNT(1) FROM pg_roles WHERE rolname = ?",
                Integer.class,
                dbUser
        );
        if (roleExists == 0) {
            jdbc.execute("CREATE ROLE " + dbUser + " LOGIN PASSWORD '" + dbPassword + "';");
        }

        jdbc.execute("ALTER DATABASE " + dbName + " OWNER TO " + dbUser + ";");
        jdbc.execute("GRANT ALL PRIVILEGES ON DATABASE " + dbName + " TO " + dbUser + ";");
    }

    @Override
    public void dropDatabaseAndUser(String engine, String dbName, String dbUser) throws Exception {
        JdbcTemplate jdbc = engineAdminService.getJdbcForEngine(2L);
        jdbc.execute("DROP DATABASE IF EXISTS " + dbName + ";");
        jdbc.execute("DROP ROLE IF EXISTS " + dbUser + ";");
    }

    @Override
    public void lockUser(String engine, String dbUser) throws Exception {
        JdbcTemplate jdbc = engineAdminService.getJdbcForEngine(2L);
        jdbc.execute("ALTER ROLE " + dbUser + " NOLOGIN;");
    }

    @Override
    public void unlockUser(String engine, String dbUser) throws Exception {
        JdbcTemplate jdbc = engineAdminService.getJdbcForEngine(2L);
        jdbc.execute("ALTER ROLE " + dbUser + " LOGIN;");
    }

    @Override
    public void rotateUserPassword(String engine, String dbUser, String newPassword) throws Exception {
        JdbcTemplate jdbc = engineAdminService.getJdbcForEngine(2L);
        jdbc.execute("ALTER ROLE " + dbUser + " WITH PASSWORD '" + newPassword + "';");
    }
}
