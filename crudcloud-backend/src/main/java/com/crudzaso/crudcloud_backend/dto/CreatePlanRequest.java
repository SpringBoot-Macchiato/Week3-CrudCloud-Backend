package com.crudzaso.crudcloud_backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePlanRequest {
    private String name;
    private int maxInstances;
    private BigDecimal priceAmount; // in cents
    private String description;
    private String state; // "ACTIVE" o "INACTIVE"
}