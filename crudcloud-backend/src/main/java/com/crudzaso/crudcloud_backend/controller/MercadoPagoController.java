package com.crudzaso.crudcloud_backend.controller;

import com.crudzaso.crudcloud_backend.service.MercadoPagoService;
import com.mercadopago.resources.preference.Preference;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin("*")
public class MercadoPagoController {

    private final MercadoPagoService service;

    public MercadoPagoController(MercadoPagoService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public Map<String, Object> createPreference(@RequestBody Map<String, Object> body) throws Exception {
        String title = (String) body.get("title");
        BigDecimal price = new BigDecimal(body.get("price").toString());
        int quantity = Integer.parseInt(body.get("quantity").toString());

        Preference preference = service.crearPreferencia(title, price, quantity);
        return Map.of("preferenceId", preference.getId());
    }
}
