package com.crudzaso.crudcloud_backend.service.impl;

import com.crudzaso.crudcloud_backend.service.DatabaseAdminService;
import com.crudzaso.crudcloud_backend.service.EngineAdminService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MysqlDatabaseAdminService implements DatabaseAdminService {

    private final EngineAdminService engineAdminService;

    public MysqlDatabaseAdminService(EngineAdminService engineAdminService) {
        this.engineAdminService = engineAdminService;
    }

    @Override
    @Transactional
    public void createDatabaseAndUser(String engine, String dbName, String dbUser, String dbPassword) throws Exception {
        JdbcTemplate jdbc = engineAdminService.getJdbcForEngine(1L);
        jdbc.execute("CREATE DATABASE IF NOT EXISTS " + dbName + ";");
        jdbc.execute("CREATE USER IF NOT EXISTS '" + dbUser + "'@'%' IDENTIFIED BY '" + dbPassword + "';");
        jdbc.execute("GRANT ALL PRIVILEGES ON " + dbName + ".* TO '" + dbUser + "'@'%';");
        jdbc.execute("FLUSH PRIVILEGES;");
    }

    @Override
    @Transactional
    public void dropDatabaseAndUser(String engine, String dbName, String dbUser) throws Exception {
        JdbcTemplate jdbc = engineAdminService.getJdbcForEngine(1L);
        jdbc.execute("DROP DATABASE IF EXISTS " + dbName + ";");
        jdbc.execute("DROP USER IF EXISTS '" + dbUser + "'@'%';");
        jdbc.execute("FLUSH PRIVILEGES;");
    }

    @Override
    public void lockUser(String engine, String dbUser) throws Exception {
        JdbcTemplate jdbc = engineAdminService.getJdbcForEngine(1L);
        jdbc.execute("ALTER USER '" + dbUser + "'@'%' ACCOUNT LOCK;");
    }

    @Override
    public void unlockUser(String engine, String dbUser) throws Exception {
        JdbcTemplate jdbc = engineAdminService.getJdbcForEngine(1L);
        jdbc.execute("ALTER USER '" + dbUser + "'@'%' ACCOUNT UNLOCK;");
    }

    @Override
    public void rotateUserPassword(String engine, String dbUser, String newPassword) throws Exception {
        JdbcTemplate jdbc = engineAdminService.getJdbcForEngine(1L);
        jdbc.execute("ALTER USER '" + dbUser + "'@'%' IDENTIFIED BY '" + newPassword + "';");
    }
}
