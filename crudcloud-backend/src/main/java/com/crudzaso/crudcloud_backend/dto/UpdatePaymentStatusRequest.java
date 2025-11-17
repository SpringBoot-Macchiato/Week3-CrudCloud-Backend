package com.crudzaso.crudcloud_backend.dto;

import lombok.Data;
import com.crudzaso.crudcloud_backend.model.PaymentStatus;

@Data
public class UpdatePaymentStatusRequest {
    private PaymentStatus status; // APPROVED or FAILED
}

