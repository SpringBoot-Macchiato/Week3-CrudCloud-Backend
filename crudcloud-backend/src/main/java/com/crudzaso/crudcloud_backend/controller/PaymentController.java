package com.crudzaso.crudcloud_backend.controller;

import com.crudzaso.crudcloud_backend.dto.CreatePaymentRequest;
import com.crudzaso.crudcloud_backend.dto.PaymentDto;
import com.crudzaso.crudcloud_backend.dto.UpdatePaymentStatusRequest;
import com.crudzaso.crudcloud_backend.model.PaymentStatus;
import com.crudzaso.crudcloud_backend.service.PaymentService;
import com.crudzaso.crudcloud_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Payments", description = "Payment management endpoints")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    @Operation(summary = "Create a payment (PENDING)")
    @PostMapping
    public ResponseEntity<PaymentDto> create(Authentication auth, @RequestBody CreatePaymentRequest req) {
        Long userId = userService.findByEmail((String) auth.getPrincipal()).getId();
        return ResponseEntity.status(201).body(paymentService.createPayment(userId, req));
    }

    @Operation(summary = "Update payment status to APPROVED or FAILED")
    @PatchMapping("/{id}/status")
    public ResponseEntity<PaymentDto> updateStatus(@PathVariable Long id, @RequestBody UpdatePaymentStatusRequest req) {
        return ResponseEntity.ok(paymentService.updateStatus(id, req));
    }

    @Operation(summary = "List payments of authenticated user")
    @GetMapping("/my")
    public ResponseEntity<List<PaymentDto>> myPayments(Authentication auth) {
        Long userId = userService.findByEmail((String) auth.getPrincipal()).getId();
        return ResponseEntity.ok(paymentService.listByUser(userId));
    }

    @Operation(summary = "List payments by status (ADMIN use)")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentDto>> listByStatus(@PathVariable PaymentStatus status) {
        return ResponseEntity.ok(paymentService.listByStatus(status));
    }
}

