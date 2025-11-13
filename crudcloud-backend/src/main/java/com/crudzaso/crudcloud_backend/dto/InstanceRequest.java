package com.crudzaso.crudcloud_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO used to create an instance.
 * For Free plan, dbName may be null (backend will generate).
 */
@Data
public class InstanceRequest {

    @NotNull(message = "engineId is required")
    private Long engineId; // e.g. id that references MySQL engine row

    @Size(min = 3, max = 63, message = "dbName must be 3..63 chars")
    private String dbName; // optional for free plan

    // Optional: desired DB username (if plan allows). If null, backend will generate.
    @Size(min = 3, max = 32, message = "userDb must be 3..32 chars")
    private String userDb;
}
