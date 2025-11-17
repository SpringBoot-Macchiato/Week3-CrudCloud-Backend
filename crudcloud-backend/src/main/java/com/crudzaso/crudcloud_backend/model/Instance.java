package com.crudzaso.crudcloud_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    // Owner of the instance (FK -> users.id)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Relation to User (read-only, uses the same column user_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private User user;

    // Engine FK (conceptual engine catalog)
    @Column(name = "engine_id", nullable = false)
    private Long engineId;

    // Relation to Engine (read-only, uses the same column engine_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "engine_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Engine engine;

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

    // optional container relation omitted for now
    @Column(name = "container_id")
    private String containerId;

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
