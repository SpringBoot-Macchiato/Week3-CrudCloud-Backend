package com.crudzaso.crudcloud_backend.controller;

import com.crudzaso.crudcloud_backend.service.MercadoPagoService;
import com.crudzaso.crudcloud_backend.service.UserService;
import com.mercadopago.resources.preference.Preference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Mercado Pago", description = "Checkout and webhook endpoints")
@RestController
@RequestMapping("/api/payments")
@CrossOrigin("*")
public class MercadoPagoController {

    private final MercadoPagoService service;
    private final UserService userService;

    public MercadoPagoController(MercadoPagoService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    // Crear preferencia por planId (requiere usuario autenticado)
    // Flujo:
    // 1. Se valida autenticación.
    // 2. Se obtiene planId del body.
    // 3. Se resuelve userId buscando el usuario por email extraído del token JWT.
    // 4. Se delega a MercadoPagoService que registra el Payment y construye la preferencia.
    // 5. Se devuelve el preferenceId para inicializar el checkout en el front.
    @Operation(summary = "Create checkout preference by planId (requires auth)")
    @PostMapping("/create/plan")
    public ResponseEntity<Map<String, Object>> createPlanPreference(Authentication auth, @RequestBody Map<String, Object> body) {
        try {
            if (auth == null) throw new SecurityException("Autenticación requerida");
            if (!body.containsKey("planId")) throw new IllegalArgumentException("Falta planId en el body");

            Long planId = Long.valueOf(body.get("planId").toString());

            // El principal en el contexto de seguridad es el email (establecido en JwtAuthenticationFilter).
            String email = (String) auth.getPrincipal();

            // Resolver userId usando UserService. Si no existe el usuario se lanza excepción.
            Long userId = userService.findByEmail(email).getId();

            Preference preference = service.createPreferenceByPlanId(planId, userId);
            return ResponseEntity.ok(Map.of(
                    "preferenceId", preference.getId(),
                    "initPoint", preference.getInitPoint()
            ));
        } catch (SecurityException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Error al crear preferencia de pago",
                    "details", e.getMessage() != null ? e.getMessage() : "Error desconocido"
            ));
        }
    }

    // Webhook de Mercado Pago: recibe notificaciones. La lógica detallada está en MercadoPagoService.
    @Operation(summary = "Webhook notifications receiver")
    @PostMapping("/notifications")
    public ResponseEntity<Void> receiveWebhook(@RequestBody Map<String, Object> data) {
        return service.receiveWebhook(data);
    }
}
