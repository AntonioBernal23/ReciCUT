package com.campania_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
public class PuntosClient {

    private final RestClient restClient;

    public PuntosClient(@Value("${recompensas.url:http://recompensas-service:8062}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public void acreditar(Long usuarioId, int cantidad, String referencia, String descripcion) {
        try {
            restClient.post()
                    .uri("/internal/puntos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "usuarioId", usuarioId,
                            "cantidad", cantidad,
                            "referencia", referencia,
                            "descripcion", descripcion))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No se pudieron asignar los puntos. Intenta de nuevo");
        }
    }
}