package com.crudzaso.crudcloud_backend.service;

public interface DatabaseAdminService {
    void createDatabaseAndUser(String engine, String dbName, String dbUser, String dbPassword) throws Exception;
    void dropDatabaseAndUser(String engine, String dbName, String dbUser) throws Exception;
    void lockUser(String engine, String dbUser) throws Exception;
    void unlockUser(String engine, String dbUser) throws Exception;
    void rotateUserPassword(String engine, String dbUser, String newPassword) throws Exception;
}

