package com.bcsystems.barberia_api.config;

import com.bcsystems.barberia_api.auth.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Solo login y refresh son publicos; el resto de /api/auth exige token.
                        .requestMatchers("/api/auth/login", "/api/auth/refresh-token").permitAll()
                        .requestMatchers("/api/auth/**").authenticated()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                        // El gate por URL deja pasar a todo el modulo; el permiso exacto de cada
                        // accion se valida con @PreAuthorize en los controllers.
                        .requestMatchers("/api/dashboard/**").hasAuthority("DASHBOARD_VER")
                        .requestMatchers("/api/clientes/**").hasAnyAuthority("CLIENTES_VER", "CLIENTES_CREAR", "CLIENTES_EDITAR", "CLIENTES_ELIMINAR")
                        .requestMatchers("/api/empleados/**").hasAnyAuthority("EMPLEADOS_VER", "EMPLEADOS_CREAR", "EMPLEADOS_EDITAR", "EMPLEADOS_ELIMINAR")
                        .requestMatchers("/api/servicios/**").hasAnyAuthority("SERVICIOS_VER", "SERVICIOS_CREAR", "SERVICIOS_EDITAR", "SERVICIOS_ELIMINAR")
                        .requestMatchers("/api/productos/**").hasAnyAuthority("PRODUCTOS_VER", "PRODUCTOS_CREAR", "PRODUCTOS_EDITAR", "PRODUCTOS_ELIMINAR")
                        .requestMatchers("/api/inventario/**").hasAnyAuthority("INVENTARIO_VER", "INVENTARIO_CREAR", "INVENTARIO_ELIMINAR")
                        .requestMatchers("/api/citas/**").hasAnyAuthority("CITAS_VER", "CITAS_CREAR", "CITAS_EDITAR", "CITAS_ELIMINAR")
                        .requestMatchers("/api/ventas/**").hasAnyAuthority("VENTAS_VER", "VENTAS_CREAR", "VENTAS_CANCELAR")
                        .requestMatchers("/api/cajas/**").hasAnyAuthority("CAJA_VER", "CAJA_APERTURA", "CAJA_INGRESOS", "CAJA_EGRESOS", "CAJA_CIERRE", "CAJA_CORTE", "VENTAS_CREAR")
                        .requestMatchers("/api/cortes/**").hasAnyAuthority("CAJA_CORTE", "COMISIONES_VER")
                        .requestMatchers("/api/comisiones/**").hasAnyAuthority("COMISIONES_VER", "COMISIONES_PAGAR", "COMISIONES_EDITAR", "COMISIONES_ELIMINAR", "CAJA_CORTE")
                        .requestMatchers("/api/configuracion/**").hasAnyAuthority("CONFIGURACION_VER", "CONFIGURACION_EDITAR", "VENTAS_VER", "VENTAS_CREAR", "CAJA_VER", "CAJA_CORTE")
                        .requestMatchers("/api/carrito/**").hasAnyAuthority("CARRITO_VER", "CARRITO_EDITAR", "VENTAS_CREAR")
                        .requestMatchers("/api/usuarios/**").hasAnyAuthority("USUARIOS_VER", "USUARIOS_CREAR", "USUARIOS_EDITAR", "USUARIOS_ELIMINAR", "ROLES_CREAR", "ROLES_EDITAR")
                        .requestMatchers("/api/roles/**").hasAnyAuthority("ROLES_VER", "ROLES_CREAR", "ROLES_EDITAR", "ROLES_ELIMINAR", "USUARIOS_VER", "USUARIOS_CREAR", "USUARIOS_EDITAR")
                        .requestMatchers("/api/permisos/**").hasAnyAuthority("PERMISOS_VER", "PERMISOS_EDITAR", "ROLES_CREAR", "ROLES_EDITAR")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(
                                    "{\"error\": \"unauthorized\", \"message\": \"Debes iniciar sesión\"}");
                        })
                        .accessDeniedHandler((request, response, deniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(
                                    "{\"error\": \"forbidden\", \"message\": \"No tienes permisos para esta acción\"}");
                        })
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
