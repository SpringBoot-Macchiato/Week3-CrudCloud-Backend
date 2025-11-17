package com.crudzaso.crudcloud_backend.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

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

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // "ADMIN" - "USER"

    @NotBlank(message = "Full name is required")
    @Column(nullable = false)
    private String fullName;

    @Builder.Default
    @Column(nullable = false)
    private boolean enable = true;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UsersPlans> usersPlans = new HashSet<>();
}
