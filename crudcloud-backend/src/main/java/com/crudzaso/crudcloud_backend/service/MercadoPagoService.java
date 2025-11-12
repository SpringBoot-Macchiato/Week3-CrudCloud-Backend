package com.crudzaso.crudcloud_backend.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class MercadoPagoService {

    public MercadoPagoService(@Value("${mercadopago.access.token}") String accessToken) {
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    public Preference crearPreferencia(String titulo, BigDecimal precio, int cantidad) throws Exception {
        PreferenceItemRequest item = PreferenceItemRequest.builder()
                .title(titulo)
                .quantity(cantidad)
                .unitPrice(precio)
                .currencyId("COP")
                .build();

        PreferenceRequest request = PreferenceRequest.builder()
                .items(List.of(item))
                .build();

        PreferenceClient client = new PreferenceClient();
        return client.create(request);
    }

    @PostMapping("/notifications")
    public ResponseEntity<Void> receiveWebhook(@RequestBody Map<String, Object> data) {
        System.out.println("Webhook recibido: " + data);
        return ResponseEntity.ok().build();
    }
}
