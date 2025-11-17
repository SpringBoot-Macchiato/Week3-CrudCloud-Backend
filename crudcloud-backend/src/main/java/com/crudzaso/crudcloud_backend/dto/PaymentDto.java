package com.crudzaso.crudcloud_backend.dto;

import com.crudzaso.crudcloud_backend.model.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class PaymentDto {
    private Long id;
    private Long userId;
    private Long planId;
    private BigDecimal amount;
    private String mercadopagoPaymentId;
    private PaymentStatus status;
    private Instant createdAt;
}

