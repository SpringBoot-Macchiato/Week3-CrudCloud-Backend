package com.crudzaso.crudcloud_backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdatePlanRequest {
    private String name;
    private Integer maxInstances;
    private String description;
    private String state;
    private BigDecimal priceAmount; // añadido para actualizar precio
}