package com.bcsystems.barberia_api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Value("${wasenderapi.api-key}")
    private String apiKey;

    @Value("${wasenderapi.api-url}")
    private String apiUrl;

    @Async
    public void sendAppointmentConfirmation(String phone, String clientName, String employeeName,
                                            LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                            List<String> servicios, Double total) {
        try {
            String normalizedPhone = normalizePhone(phone);
            if (normalizedPhone == null) {
                log.warn("Telefono invalido para el cliente {}: {}", clientName, phone);
                return;
            }

            String serviciosStr = String.join(", ", servicios);
            String totalStr = total != null ? String.format("%.2f", total) : "0.00";

            String message = String.format(
                """
                \u00a1Hola %s! \uD83C\uDF89\n
                Tu cita en la barber\u00eda fue confirmada:\n
                \uD83D\uDCC5 Fecha: %s
                \u23F0 Horario: %s - %s
                \u2702\uFE0F Servicios: %s
                \uD83D\uDC88 Barbero: %s
                \uD83D\uDCB0 Total: $%s\n
                \u00a1Te esperamos!""",
                clientName,
                fechaInicio.format(DATE_FMT),
                fechaInicio.format(TIME_FMT),
                fechaFin.format(TIME_FMT),
                serviciosStr,
                employeeName,
                totalStr
            );

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, String> body = Map.of(
                "to", normalizedPhone,
                "text", message
            );

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(apiUrl, request, Map.class);

            log.info("WhatsApp enviado correctamente a {} ({})", clientName, normalizedPhone);
        } catch (Exception e) {
            log.error("Error al enviar WhatsApp a {}: {}", clientName, e.getMessage());
        }
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) return null;
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() == 10) {
            return "+52" + digits;
        }
        if (digits.length() > 10 && !phone.startsWith("+")) {
            return "+" + digits;
        }
        if (phone.startsWith("+")) {
            return phone;
        }
        return digits;
    }
}
