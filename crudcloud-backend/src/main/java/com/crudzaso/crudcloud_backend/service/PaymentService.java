package com.crudzaso.crudcloud_backend.service;

import com.crudzaso.crudcloud_backend.dto.CreatePaymentRequest;
import com.crudzaso.crudcloud_backend.dto.PaymentDto;
import com.crudzaso.crudcloud_backend.dto.UpdatePaymentStatusRequest;
import com.crudzaso.crudcloud_backend.model.Payment;
import com.crudzaso.crudcloud_backend.model.PaymentStatus;
import com.crudzaso.crudcloud_backend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentDto createPayment(Long userId, CreatePaymentRequest req) {
        Payment p = Payment.builder()
                .userId(userId)
                .planId(req.getPlanId())
                .amount(req.getAmount())
                .mercadopagoPaymentId(req.getMercadopagoPaymentId())
                .status(PaymentStatus.PENDING)
                .build();
        return toDto(paymentRepository.save(p));
    }

    public PaymentDto updateStatus(Long paymentId, UpdatePaymentStatusRequest req) {
        Payment p = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        if (req.getStatus() == PaymentStatus.PENDING) {
            throw new IllegalArgumentException("Cannot set status back to PENDING");
        }
        p.setStatus(req.getStatus());
        return toDto(paymentRepository.save(p));
    }

    public List<PaymentDto> listByUser(Long userId) {
        return paymentRepository.findByUserId(userId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<PaymentDto> listByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    private PaymentDto toDto(Payment p) {
        PaymentDto dto = new PaymentDto();
        dto.setId(p.getId());
        dto.setUserId(p.getUserId());
        dto.setPlanId(p.getPlanId());
        dto.setAmount(p.getAmount());
        dto.setMercadopagoPaymentId(p.getMercadopagoPaymentId());
        dto.setStatus(p.getStatus());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }
}

