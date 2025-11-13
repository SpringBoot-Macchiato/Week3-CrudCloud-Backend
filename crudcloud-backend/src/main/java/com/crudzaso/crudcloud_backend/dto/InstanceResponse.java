package com.crudzaso.crudcloud_backend.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class InstanceResponse {
    private Long id;
    private Long userId;
    private Long engineId;
    private String dbName;
    private String userDb;
    // password is returned ONLY on creation flow; otherwise null
    private String password;
    private String host;
    private Integer port;
    private String state;
    private Boolean passwordShown;
    private Instant createdAt;
    private Instant updatedAt;
}
