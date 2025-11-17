package com.crudzaso.crudcloud_backend.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.preference.Preference;
import com.crudzaso.crudcloud_backend.model.PaymentStatus;
import com.crudzaso.crudcloud_backend.model.Payment;
import com.crudzaso.crudcloud_backend.model.Plan;
import com.crudzaso.crudcloud_backend.model.UsersPlans;
import com.crudzaso.crudcloud_backend.model.User;
import com.crudzaso.crudcloud_backend.repository.PaymentRepository;
import com.crudzaso.crudcloud_backend.repository.PlanRepository;
import com.crudzaso.crudcloud_backend.repository.UsersPlansRepository;
import com.crudzaso.crudcloud_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class MercadoPagoService {

    private final PlanRepository planRepository;
    private final PaymentRepository paymentRepository;
    private final UsersPlansRepository usersPlansRepository;
    private final UserRepository userRepository;

    // Bases y paths configurables desde application.properties
    private final String backBaseUrl;   // https://api.macchiato.crudzaso.com
    private final String frontBaseUrl;  // https://macchiato.crudzaso.com
    private final String successPath;   // /pago-exitoso
    private final String failurePath;   // /pago-fallido
    private final String pendingPath;   // /pago-pendiente
    private final String webhookPath;   // /api/payments/notifications
    private final String currency;      // opcional: si vacío, no se envía currencyId

    public MercadoPagoService(@Value("${mercadopago.access.token}") String accessToken,
                              PlanRepository planRepository,
                              PaymentRepository paymentRepository,
                              UsersPlansRepository usersPlansRepository,
                              UserRepository userRepository,
                              @Value("${app.base-url}") String backBaseUrl,
                              @Value("${app.front-base-url}") String frontBaseUrl,
                              @Value("${app.payments.success-path}") String successPath,
                              @Value("${app.payments.failure-path}") String failurePath,
                              @Value("${app.payments.pending-path}") String pendingPath,
                              @Value("${app.payments.webhook-path}") String webhookPath,
                              @Value("${app.payments.currency:}") String currency) {
        MercadoPagoConfig.setAccessToken(accessToken);
        this.planRepository = planRepository;
        this.paymentRepository = paymentRepository;
        this.usersPlansRepository = usersPlansRepository;
        this.userRepository = userRepository;
        this.backBaseUrl = backBaseUrl;
        this.frontBaseUrl = frontBaseUrl;
        this.successPath = successPath;
        this.failurePath = failurePath;
        this.pendingPath = pendingPath;
        this.webhookPath = webhookPath;
        this.currency = currency == null ? "" : currency.trim();
    }

    // Helper para unir base y path sin duplicar barras
    private String joinUrl(String base, String path) {
        if (base.endsWith("/") && path.startsWith("/")) return base + path.substring(1);
        if (!base.endsWith("/") && !path.startsWith("/")) return base + "/" + path;
        return base + path;
    }

    // Crea una preferencia de pago por planId.
    public Preference createPreferenceByPlanId(Long planId, Long userId) throws Exception {
        try {
            Plan plan = planRepository.findById(planId)
                    .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado: " + planId));
            if (!"ACTIVE".equalsIgnoreCase(plan.getState())) {
                throw new IllegalStateException("El plan no está activo: " + planId);
            }
            BigDecimal price = plan.getPriceAmount();
            if (price == null) throw new IllegalStateException("El plan no tiene priceAmount definido: " + planId);
            if (price.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalStateException("El precio debe ser mayor que cero: " + price);
            // Asegurar 2 decimales máximo
            price = price.setScale(2, java.math.RoundingMode.HALF_UP);

            String externalRef = "pay-" + UUID.randomUUID();
            Payment payment = Payment.builder()
                    .userId(userId)
                    .planId(planId)
                    .amount(price)
                    .status(PaymentStatus.PENDING)
                    .externalReference(externalRef)
                    .build();
            paymentRepository.save(payment);

            // Construir el item. Si no se configuró moneda, no enviamos currencyId para que MP tome la de la cuenta
            PreferenceItemRequest.PreferenceItemRequestBuilder itemBuilder = PreferenceItemRequest.builder()
                    .title(plan.getName())
                    .quantity(1)
                    .unitPrice(price);
            if (!this.currency.isEmpty()) {
                itemBuilder.currencyId(this.currency);
            }
            PreferenceItemRequest item = itemBuilder.build();

            String successUrl = joinUrl(frontBaseUrl, successPath);
            String failureUrl = joinUrl(frontBaseUrl, failurePath);
            String pendingUrl = joinUrl(frontBaseUrl, pendingPath);
            String notificationUrl = joinUrl(backBaseUrl, webhookPath);

            System.out.println("=== DEBUG MERCADOPAGO ===");
            System.out.println("PlanId=" + planId + " userId=" + userId);
            System.out.println("Nombre Plan=" + plan.getName());
            System.out.println("Precio=" + price);
            System.out.println("Success URL=" + successUrl);
            System.out.println("Failure URL=" + failureUrl);
            System.out.println("Pending URL=" + pendingUrl);
            System.out.println("Notification URL=" + notificationUrl);
            System.out.println("ExternalRef=" + externalRef);
            System.out.println("Moneda configurada=" + (this.currency.isEmpty() ? "(por defecto de cuenta)" : this.currency));

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(successUrl)
                    .failure(failureUrl)
                    .pending(pendingUrl)
                    .build();

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(List.of(item))
                    .backUrls(backUrls)
                    .notificationUrl(notificationUrl)
                    .metadata(Map.of("userId", userId, "planId", planId))
                    .externalReference(externalRef)
                    .autoReturn("approved")
                    .build();

            return new PreferenceClient().create(request);
        } catch (com.mercadopago.exceptions.MPApiException e) {
            System.err.println("=== ERROR MERCADOPAGO API ===");
            System.err.println("Status Code: " + e.getStatusCode());
            System.err.println("Message: " + e.getMessage());
            try {
                var apiResp = e.getApiResponse();
                if (apiResp != null) {
                    System.err.println("Raw Response object: " + apiResp); // usa toString()
                    System.err.println("Headers: " + apiResp.getHeaders());
                }
            } catch (Exception ignore) {}
            throw new RuntimeException("Error al crear preferencia en Mercado Pago: " + e.getMessage(), e);
        }
    }

    // Webhook Mercado Pago: procesa notificaciones.
    public ResponseEntity<Void> receiveWebhook(Map<String, Object> data) {
        System.out.println("Webhook recibido: " + data);
        try {
            String type = (String) data.get("type");
            if (!"payment".equalsIgnoreCase(type)) return ResponseEntity.ok().build();
            Object obj = data.get("data");
            if (!(obj instanceof Map<?,?> map)) return ResponseEntity.ok().build();
            Object idObj = map.get("id");
            if (idObj == null) return ResponseEntity.ok().build();

            Long mpPaymentId = Long.valueOf(idObj.toString());
            com.mercadopago.resources.payment.Payment mpPayment = new PaymentClient().get(mpPaymentId);
            if (mpPayment == null) return ResponseEntity.ok().build();

            String externalRef = mpPayment.getExternalReference();
            if (externalRef == null) return ResponseEntity.ok().build();

            Optional<Payment> optLocal = paymentRepository.findByExternalReference(externalRef);
            if (optLocal.isEmpty()) return ResponseEntity.ok().build();

            Payment local = optLocal.get();
            if (local.getStatus() == PaymentStatus.APPROVED || local.getStatus() == PaymentStatus.FAILED) {
                return ResponseEntity.ok().build();
            }

            String mpStatus = mpPayment.getStatus();
            PaymentStatus newStatus = switch (mpStatus == null ? "" : mpStatus.toLowerCase()) {
                case "approved" -> PaymentStatus.APPROVED;
                case "pending" -> PaymentStatus.PENDING;
                case "rejected", "cancelled", "refunded", "charged_back" -> PaymentStatus.FAILED;
                default -> PaymentStatus.PENDING;
            };

            local.setMercadopagoPaymentId(String.valueOf(mpPaymentId));
            local.setStatus(newStatus);
            paymentRepository.save(local);

            if (newStatus == PaymentStatus.APPROVED) {
                activateUserPlan(local.getUserId(), local.getPlanId());
            }
        } catch (Exception e) {
            System.out.println("Error procesando webhook: " + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    // Activate or extend a user's plan:
    // - If the user already has an ACTIVE subscription for the same plan and it is not expired, extend endDate by +30 days.
    // - Otherwise, deactivate any current ACTIVE subscriptions and create a new ACTIVE for 30 days.
    private void activateUserPlan(Long userId, Long planId) {
        Date now = new Date();

        // Fetch all current ACTIVE subscriptions for the user
        List<UsersPlans> activos = usersPlansRepository.findByUserIdAndStatus(userId, "ACTIVE");

        // Load required entities
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found when activating: " + planId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // 1) Try to EXTEND if there is an ACTIVE of the same plan that is not expired
        UsersPlans mismoPlanActivo = null;
        for (UsersPlans up : activos) {
            if (up.getPlan() != null && Objects.equals(up.getPlan().getId(), planId)) {
                mismoPlanActivo = up;
                break;
            }
        }
        if (mismoPlanActivo != null) {
            Date end = mismoPlanActivo.getEndDate();
            // Consider it "valid" if endDate is null or in the future
            boolean vigente = (end == null) || end.after(now);
            if (vigente) {
                // Extend by 30 additional days
                long base = (end != null ? end.getTime() : now.getTime());
                Date nuevaEnd = new Date(base + 30L * 24 * 60 * 60 * 1000);
                mismoPlanActivo.setEndDate(nuevaEnd);
                // Ensure ACTIVE status just in case
                mismoPlanActivo.setStatus("ACTIVE");
                usersPlansRepository.save(mismoPlanActivo);
                return; // Done: no new records created
            }
            // If the same plan is expired (end <= now), we will deactivate it below along with the rest
        }

        // 2) If extension does not apply, deactivate any current ACTIVE and create a new one for 30 days
        for (UsersPlans up : activos) {
            up.setStatus("INACTIVE");
            up.setEndDate(now);
        }
        if (!activos.isEmpty()) usersPlansRepository.saveAll(activos);

        // Create a new ACTIVE for 30 days starting now
        Date start = now;
        Date end = new Date(start.getTime() + 30L * 24 * 60 * 60 * 1000);

        UsersPlans nuevo = UsersPlans.builder()
                .user(user)
                .plan(plan)
                .status("ACTIVE")
                .startDate(start)
                .endDate(end)
                .build();
        usersPlansRepository.save(nuevo);
    }
}
