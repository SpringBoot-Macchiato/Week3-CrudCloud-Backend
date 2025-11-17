package com.crudzaso.crudcloud_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "engines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Engine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g., MySQL, PostgreSQL, SQLServer
}

