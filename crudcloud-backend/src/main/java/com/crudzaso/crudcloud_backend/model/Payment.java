package com.crudzaso.crudcloud_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private User user;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", referencedColumnName = "id", insertable = false, updatable = false)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private Plan plan;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "mercadopago_payment_id", nullable = true)
    private String mercadopagoPaymentId; // optional, set when confirmed

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "external_reference", length = 64, nullable = false)
    private String externalReference;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = PaymentStatus.PENDING;
    }
}
