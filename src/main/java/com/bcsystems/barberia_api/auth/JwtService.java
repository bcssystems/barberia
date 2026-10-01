package com.bcsystems.barberia_api.auth;

import com.bcsystems.barberia_api.domain.Permiso;
import com.bcsystems.barberia_api.domain.Token;
import com.bcsystems.barberia_api.domain.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Token generateToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("nombre", usuario.getNombre());
        claims.put("rol", usuario.getRol().getNombre());
        claims.put("permisos", new ArrayList<>(getAllPermissions(usuario)));

        String accessToken = buildToken(claims, usuario.getUsuario(), jwtExpiration);
        String refreshToken = buildToken(new HashMap<>(), usuario.getUsuario(), refreshExpiration);

        Token token = new Token();
        token.setToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setType("BEARER");
        token.setRevoked(false);
        token.setExpired(false);
        token.setUsuario(usuario);
        return token;
    }

    private Set<String> getAllPermissions(Usuario usuario) {
        if (usuario.getRol() == null || usuario.getRol().getPermisos() == null) {
            return new LinkedHashSet<>();
        }
        return usuario.getRol().getPermisos().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .map(Permiso::getClave)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<String> extractPermissions(String token) {
        Claims claims = extractAllClaims(token);
        List<String> permisos = claims.get("permisos", List.class);
        return permisos != null ? new LinkedHashSet<>(permisos) : new LinkedHashSet<>();
    }

    private String buildToken(Map<String, Object> extraClaims, String username, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, Usuario usuario) {
        final String username = extractUsername(token);
        return username.equalsIgnoreCase(usuario.getUsuario()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

}
