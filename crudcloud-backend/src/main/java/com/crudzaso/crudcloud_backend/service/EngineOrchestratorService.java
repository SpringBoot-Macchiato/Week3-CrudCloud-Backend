package com.crudzaso.crudcloud_backend.service;

import com.crudzaso.crudcloud_backend.service.impl.MysqlDatabaseAdminService;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class EngineOrchestratorService implements DatabaseAdminService {

    private final Map<String, DatabaseAdminService> engines = new HashMap<>();

    public EngineOrchestratorService(MysqlDatabaseAdminService mysqlService) {
        engines.put("MySQL", mysqlService);
        // engines.put("PostgreSQL", postgresService);
        // engines.put("SQLServer", sqlServerService);
    }

    private DatabaseAdminService getService(String engine) {
        DatabaseAdminService service = engines.get(engine);
        if (service == null) throw new IllegalArgumentException("Unsupported engine: " + engine);
        return service;
    }

    @Override
    public void createDatabaseAndUser(String engine, String dbName, String dbUser, String dbPassword) throws Exception {
        getService(engine).createDatabaseAndUser(engine, dbName, dbUser, dbPassword);
    }

    @Override
    public void dropDatabaseAndUser(String engine, String dbName, String dbUser) throws Exception {
        getService(engine).dropDatabaseAndUser(engine, dbName, dbUser);
    }

    @Override
    public void lockUser(String engine, String dbUser) throws Exception {
        getService(engine).lockUser(engine, dbUser);
    }

    @Override
    public void unlockUser(String engine, String dbUser) throws Exception {
        getService(engine).unlockUser(engine, dbUser);
    }

    @Override
    public void rotateUserPassword(String engine, String dbUser, String newPassword) throws Exception {
        getService(engine).rotateUserPassword(engine, dbUser, newPassword);
    }
}
