package com.bcsystems.barberia_api.auth;

import com.bcsystems.barberia_api.domain.Token;
import com.bcsystems.barberia_api.domain.Usuario;
import com.bcsystems.barberia_api.repository.TokenRepository;
import com.bcsystems.barberia_api.repository.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UsuarioRepository usuarioRepository,
                       TokenRepository tokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.usuario(), request.password()));
        } catch (DisabledException e) {
            throw new BadCredentialsException("El usuario está inactivo");
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }

        Usuario usuario = usuarioRepository.findByUsuarioIgnoreCase(request.usuario())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        revokeAllTokens(usuario);

        Token token = jwtService.generateToken(usuario);
        tokenRepository.save(token);

        return buildAuthResponse(token, usuario);
    }

    @Transactional
    public AuthResponse refreshToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadCredentialsException("Token de refresco inválido");
        }

        final String refreshToken = authHeader.substring(7);
        final String username;
        try {
            username = jwtService.extractUsername(refreshToken);
        } catch (Exception e) {
            throw new BadCredentialsException("Token de refresco inválido");
        }

        Usuario usuario = usuarioRepository.findByUsuarioIgnoreCase(username)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new BadCredentialsException("El usuario está inactivo");
        }

        Token storedToken = tokenRepository.findByRefreshToken(refreshToken).orElse(null);
        if (storedToken == null || Boolean.TRUE.equals(storedToken.getRevoked()) || Boolean.TRUE.equals(storedToken.getExpired())) {
            throw new BadCredentialsException("Token de refresco inválido o expirado");
        }

        if (jwtService.isTokenExpired(refreshToken)) {
            storedToken.setExpired(true);
            tokenRepository.save(storedToken);
            throw new BadCredentialsException("Token de refresco expirado");
        }

        revokeAllTokens(usuario);

        Token newToken = jwtService.generateToken(usuario);
        tokenRepository.save(newToken);

        return buildAuthResponse(newToken, usuario);
    }

    @Transactional
    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            final String token = authHeader.substring(7);
            tokenRepository.findByToken(token).ifPresent(storedToken -> {
                storedToken.setRevoked(true);
                storedToken.setExpired(true);
                tokenRepository.save(storedToken);
            });
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse me(String authHeader, String username) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadCredentialsException("Token inválido");
        }

        final String token = authHeader.substring(7);
        Usuario usuario = usuarioRepository.findByUsuarioIgnoreCase(username)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new BadCredentialsException("El usuario está inactivo");
        }

        Token storedToken = tokenRepository.findByToken(token).orElse(null);
        if (storedToken == null || Boolean.TRUE.equals(storedToken.getRevoked()) || Boolean.TRUE.equals(storedToken.getExpired())) {
            throw new BadCredentialsException("Sesión inválida o cerrada");
        }

        return buildAuthResponse(storedToken, usuario);
    }

    private void revokeAllTokens(Usuario usuario) {
        List<Token> tokens = tokenRepository.findByUsuarioIdUsuarioAndRevokedFalseAndExpiredFalse(usuario.getIdUsuario());
        tokens.forEach(t -> {
            t.setExpired(true);
            t.setRevoked(true);
        });
        tokenRepository.saveAll(tokens);
    }

    private AuthResponse buildAuthResponse(Token token, Usuario usuario) {
        List<String> permisos = jwtService.extractPermissions(token.getToken()).stream().sorted().toList();
        return new AuthResponse(
                token.getToken(),
                token.getRefreshToken(),
                "Bearer",
                usuario.getUsuario(),
                usuario.getNombre(),
                usuario.getRol() != null ? usuario.getRol().getNombre() : null,
                permisos
        );
    }

}
