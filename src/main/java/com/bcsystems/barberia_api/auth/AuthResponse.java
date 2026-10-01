package com.bcsystems.barberia_api.auth;

import java.util.List;

public record AuthResponse(
        String token,
        String refreshToken,
        String tokenType,
        String usuario,
        String nombre,
        String rol,
        List<String> permisos
) {
}
