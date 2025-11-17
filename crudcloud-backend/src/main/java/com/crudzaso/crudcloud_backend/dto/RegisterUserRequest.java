package com.crudzaso.crudcloud_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUserRequest {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be 6..100 chars")
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    // Optional: allow setting role explicitly; default to USER if null
    private String role;
}

