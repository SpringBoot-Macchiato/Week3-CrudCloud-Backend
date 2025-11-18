package com.crudzaso.crudcloud_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleAuthRequest {
    @NotBlank(message = "Google token is required")
    private String token; // ID token de Google desde el frontend
}
