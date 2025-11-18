package com.crudzaso.crudcloud_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true) // Nullable para usuarios de Google (sin password)
    private String password;

    @Column(nullable = false)
    private String role; // "ADMIN" - "USER"

    @Column(nullable = false)
    private String fullName;

    // Campos para autenticación con Google
    @Column(unique = true)
    private String googleId;

    @Column
    private String provider; // "LOCAL", "GOOGLE"

    @Column
    private String picture; // URL de la foto de perfil de Google
}
