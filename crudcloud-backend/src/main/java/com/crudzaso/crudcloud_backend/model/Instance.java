package com.crudzaso.crudcloud_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "instances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Instance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owner of the instance
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "engine_id", nullable = false)
    private Long engineId;

    @Column(name = "db_name", nullable = false)
    private String dbName;

    @Column(name = "user_db", nullable = false)
    private String userDb;

    @Column(name = "password_encrypted", nullable = false, length = 1024)
    private String passwordEncrypted;

    @Column(name = "host")
    private String host; // host/IP of MySQL server (VPS)

    @Column(name = "port")
    private Integer port;

    @Column(name = "container_id")
    private String containerId; // optional if we use docker exec or keep empty

    // possible values: CREATING, RUNNING, SUSPENDED, DELETED
    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "password_shown_boolean")
    private Boolean passwordShown;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
