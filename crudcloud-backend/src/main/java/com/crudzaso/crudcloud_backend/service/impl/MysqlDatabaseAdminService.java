package com.crudzaso.crudcloud_backend.service.impl;

import com.crudzaso.crudcloud_backend.service.DatabaseAdminService;
import com.crudzaso.crudcloud_backend.service.EngineAdminService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JdbcDatabaseAdminService implements DatabaseAdminService {

    private final EngineAdminService engineAdminService;

    public JdbcDatabaseAdminService(EngineAdminService engineAdminService) {
        this.engineAdminService = engineAdminService;
    }

    @Override
    @Transactional
    public void createDatabaseAndUser(String dbName, String dbUser, String dbPassword) throws Exception {
        JdbcTemplate jdbc = engineAdminService.getJdbcForEngine(1L); // default MySQL engine
        jdbc.execute("CREATE DATABASE IF NOT EXISTS " + dbName + ";");
        jdbc.execute("CREATE USER IF NOT EXISTS '" + dbUser + "'@'%' IDENTIFIED BY '" + dbPassword + "';");
        jdbc.execute("GRANT ALL PRIVILEGES ON " + dbName + ".* TO '" + dbUser + "'@'%';");
        jdbc.execute("FLUSH PRIVILEGES;");
    }

    @Override
    @Transactional
    public void dropDatabaseAndUser(String dbName, String dbUser) throws Exception {
        JdbcTemplate jdbc = engineAdminService.getJdbcForEngine(1L);
        jdbc.execute("DROP DATABASE IF EXISTS " + dbName + ";");
        jdbc.execute("DROP USER IF EXISTS '" + dbUser + "'@'%';");
        jdbc.execute("FLUSH PRIVILEGES;");
    }

    @Override
    @Transactional
    public void lockUser(String dbUser) throws Exception {
        JdbcTemplate jdbc = engineAdminService.getJdbcForEngine(1L);
        jdbc.execute("ALTER USER '" + dbUser + "'@'%' ACCOUNT LOCK;");
        jdbc.execute("FLUSH PRIVILEGES;");
    }

    @Override
    @Transactional
    public void unlockUser(String dbUser) throws Exception {
        JdbcTemplate jdbc = engineAdminService.getJdbcForEngine(1L);
        jdbc.execute("ALTER USER '" + dbUser + "'@'%' ACCOUNT UNLOCK;");
        jdbc.execute("FLUSH PRIVILEGES;");
    }

    @Override
    @Transactional
    public void rotateUserPassword(String dbUser, String newPassword) throws Exception {
        JdbcTemplate jdbc = engineAdminService.getJdbcForEngine(1L);
        jdbc.execute("ALTER USER '" + dbUser + "'@'%' IDENTIFIED BY '" + newPassword + "';");
        jdbc.execute("FLUSH PRIVILEGES;");
    }
}
