package com.bcsystems.barberia_api.config;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

public class ClienteIdUtil {
    private static final String PATH = "C:/barberia/client.id";

    public static String obtenerClienteId() {
        try {
            File file = new File(PATH);
            file.getParentFile().mkdirs();

            if (file.exists()) {
                return new String(Files.readAllBytes(file.toPath()));
            }

            String nuevoId = UUID.randomUUID().toString();
            Files.write(file.toPath(), nuevoId.getBytes());

            return nuevoId;

        } catch (Exception e) {
            throw new RuntimeException("Error generando clientId");
        }
    }
}
