package com.crudzaso.crudcloud_backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {
    private Long planId;
    private BigDecimal amount;
    private String mercadopagoPaymentId; // optional initial value
}

