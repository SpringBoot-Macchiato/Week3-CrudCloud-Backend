package com.crudzaso.crudcloud_backend.service.impl;

import com.crudzaso.crudcloud_backend.service.DatabaseAdminService;
import com.crudzaso.crudcloud_backend.service.EngineAdminService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SqlServerDatabaseAdminService implements DatabaseAdminService {

    private final EngineAdminService engineAdminService;

    public SqlServerDatabaseAdminService(EngineAdminService engineAdminService) {
        this.engineAdminService = engineAdminService;
    }

    @Override
    public void createDatabaseAndUser(String engine, String dbName, String dbUser, String dbPassword) throws Exception {
        JdbcTemplate jdbc = engineAdminService.getJdbcForEngine(3L); // 3 = SQL Server

        // Crear base de datos si no existe
        jdbc.execute("IF DB_ID(N'" + dbName + "') IS NULL CREATE DATABASE [" + dbName + "];");

        // Crear LOGIN a nivel servidor si no existe
        jdbc.execute("IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = N'" + dbUser + "') " +
                "CREATE LOGIN [" + dbUser + "] WITH PASSWORD = N'" + dbPassword + "', CHECK_POLICY = OFF;");

        // Crear USER dentro de la base y asignarlo al rol db_owner
        jdbc.execute("USE [" + dbName + "]; " +
                "IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = N'" + dbUser + "') " +
                "CREATE USER [" + dbUser + "] FOR LOGIN [" + dbUser + "]; " +
                "ALTER ROLE db_owner ADD MEMBER [" + dbUser + "]; ");
    }

    @Override
    public void dropDatabaseAndUser(String engine, String dbName, String dbUser) throws Exception {
        JdbcTemplate jdbc = engineAdminService.getJdbcForEngine(3L);

        // Poner en SINGLE_USER y eliminar la base de datos si existe
        jdbc.execute("IF DB_ID(N'" + dbName + "') IS NOT NULL BEGIN " +
                "ALTER DATABASE [" + dbName + "] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; " +
                "DROP DATABASE [" + dbName + "]; END");

        // Eliminar el LOGIN si existe
        jdbc.execute("IF EXISTS (SELECT 1 FROM sys.server_principals WHERE name = N'" + dbUser + "') DROP LOGIN [" + dbUser + "];" );
    }

    @Override
    public void lockUser(String engine, String dbUser) throws Exception {
        JdbcTemplate jdbc = engineAdminService.getJdbcForEngine(3L);
        jdbc.execute("ALTER LOGIN [" + dbUser + "] DISABLE;");
    }

    @Override
    public void unlockUser(String engine, String dbUser) throws Exception {
        JdbcTemplate jdbc = engineAdminService.getJdbcForEngine(3L);
        jdbc.execute("ALTER LOGIN [" + dbUser + "] ENABLE;");
    }

    @Override
    public void rotateUserPassword(String engine, String dbUser, String newPassword) throws Exception {
        JdbcTemplate jdbc = engineAdminService.getJdbcForEngine(3L);
        jdbc.execute("ALTER LOGIN [" + dbUser + "] WITH PASSWORD = N'" + newPassword + "';");
    }
}

