package com.crudzaso.crudcloud_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO used to create an instance.
 * If dbName/userDb are blank or missing, backend will generate values.
 */
@Data
public class InstanceRequest {

    @NotNull(message = "engineId is required")
    private Long engineId; // e.g. id that references MySQL engine row

    // Allow blank ("" or only spaces) to auto-generate; otherwise require 3..63 chars
    @Pattern(
            regexp = "^\\s*$|^.{3,63}$",
            message = "dbName must be blank or 3..63 chars"
    )
    private String dbName; // optional for free plan

    // Allow blank to auto-generate; otherwise require 3..32 chars
    @Pattern(
            regexp = "^\\s*$|^.{3,32}$",
            message = "userDb must be blank or 3..32 chars"
    )
    private String userDb; // optional user name
}
