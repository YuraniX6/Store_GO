package com.storego.ratingservice.config;

import com.storego.ratingservice.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración central de seguridad del microservicio.
 *
 * Aquí se define QUÉ rutas son públicas y cuáles requieren un JWT válido,
 * y se conecta nuestro filtro personalizado (JwtAuthenticationFilter) a la
 * cadena de filtros de Spring Security.
 */
@Configuration // Indica a Spring que esta clase contiene definiciones de beans de configuración.
@EnableWebSecurity // Habilita la configuración de seguridad web personalizada de Spring Security.
@RequiredArgsConstructor // Lombok: genera un constructor con los campos "final" como parámetros (inyección por constructor).
public class SecurityConfig {

    // Filtro que se encarga de leer y validar el JWT en cada petición.
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Este método define la cadena de filtros de seguridad (SecurityFilterChain),
    // que es donde Spring Security decide cómo proteger (o no) cada endpoint.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Desactivamos CSRF porque esta es una API REST sin sesiones de navegador
                // (usamos JWT, no cookies de sesión, así que CSRF no aplica aquí).
                .csrf(csrf -> csrf.disable())
                // STATELESS significa que el servidor NO guarda sesión de usuario en memoria;
                // cada petición debe traer su propio JWT, ya que no hay "login persistente" en el servidor.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Rutas completamente públicas, sin necesidad de JWT:
                        // documentación de Swagger y el endpoint de salud (health check).
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/actuator/health")
                        .permitAll()
                        // Endpoint público de solo lectura: cualquiera puede ver el resumen
                        // de calificaciones de una skin (promedio + lista), sin loguearse.
                        // Importante: solo se permite el método GET en esta ruta; un POST
                        // a la misma ruta seguiría exigiendo autenticación.
                        .requestMatchers(HttpMethod.GET, "/ratings/skin/**")
                        .permitAll()
                        // Cualquier otra ruta (POST /ratings, GET /ratings/me, DELETE /ratings/{id})
                        // exige que el usuario esté autenticado con un JWT válido.
                        .anyRequest().authenticated())
                // Insertamos nuestro filtro JWT ANTES del filtro estándar de Spring Security,
                // para que sea nuestro filtro el que decida quién está autenticado.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // Si una petición no autenticada intenta acceder a una ruta protegida,
                // en vez de la página HTML de error por defecto, devolvemos un JSON 401 limpio.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setStatus(401);
                            response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\""
                                    + authException.getMessage() + "\"}");
                        }));

        return http.build();
    }
}