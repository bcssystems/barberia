package com.bcsystems.barberia_api.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class LicenciaService {

    private final String URL = "http://verificacion.smarttech.icu/validar";

    public boolean validarLicencia(String clienteId){
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> body = new HashMap<>();
        body.put("clienteId", clienteId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(URL, request, Map.class);

            return (Boolean) response.getBody().get("valido");
        } catch (Exception e){
            return false;
        }
    }

    public void registrarSiNoExiste(String clienteId){
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> body = new HashMap<>();
        body.put("clienteId", clienteId);
        body.put("servicio", "barberia");

        try {
            restTemplate.postForEntity(
                    "http://verificacion.smarttech.icu/registrar",
                    body,
                    String.class
            );
        } catch (Exception e){
        }
    }
}
