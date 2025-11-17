package com.crudzaso.crudcloud_backend.repository;

import com.crudzaso.crudcloud_backend.model.Payment;
import com.crudzaso.crudcloud_backend.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserId(Long userId);
    List<Payment> findByPlanId(Long planId);
    List<Payment> findByStatus(PaymentStatus status);
    Optional<Payment> findByExternalReference(String externalReference);
}
