package com.storego.ratingservice.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Filtro que se ejecuta en CADA petición HTTP que llega al microservicio,
 * antes de que llegue al Controller.
 *
 * Su trabajo es: leer el header "Authorization: Bearer <token>", validar
 * el JWT con JwtService, y si es válido, registrar al usuario como
 * "autenticado" dentro del SecurityContext de Spring Security. De esta
 * forma, más adelante en el Controller, "Authentication authentication"
 * ya tiene cargado el ID del usuario sin tener que volver a leer el token.
 *
 * OncePerRequestFilter garantiza que este filtro se ejecute una sola vez
 * por cada petición HTTP (evita ejecuciones duplicadas).
 */
@Component // Lo registra como un bean de Spring para que se pueda inyectar donde se necesite (ej. en SecurityConfig).
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");

            // Si no hay header "Authorization" o no empieza con "Bearer ",
            // simplemente dejamos pasar la petición sin autenticar. Más adelante,
            // SecurityConfig decidirá si esa ruta requiere autenticación o no.
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.debug("No Bearer token found in Authorization header");
                filterChain.doFilter(request, response);
                return;
            }

            // Se extrae el token quitando el prefijo "Bearer ".
            String token = authHeader.substring("Bearer ".length());
            log.debug("Validating JWT token");

            // Si el token no es válido (corrupto, mal firmado, etc.), se deja
            // pasar la petición SIN autenticar (igual, las rutas protegidas
            // la rechazarán más adelante con un 401).
            if (!jwtService.isTokenValid(token)) {
                log.warn("Invalid or expired JWT token");
                filterChain.doFilter(request, response);
                return;
            }

            // Se extraen los datos del usuario desde el token.
            UUID userId = jwtService.extractUserId(token);
            String username = jwtService.extractUsername(token);
            String role = jwtService.extractRole(token);

            log.debug("JWT validated successfully for user: {} ({})", userId, username);

            // Se construye la lista de "autoridades" (roles) del usuario, con
            // el prefijo "ROLE_" que espera Spring Security por convención.
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + role));

            // Se crea el objeto de autenticación de Spring Security. Se usa el
            // userId (como String) como "principal", para poder recuperarlo
            // fácilmente después en el Controller con Authentication.getPrincipal().
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId.toString(),
                    null, // No se necesita password aquí, ya viene validado por el JWT.
                    authorities);

            // Se guarda la autenticación en el contexto de seguridad de Spring,
            // disponible durante toda la duración de esta petición HTTP.
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("SecurityContext set for user: {}", userId);

        } catch (Exception e) {
            // Si algo falla al procesar el token, no se autentica al usuario
            // y se limpia el contexto de seguridad por seguridad.
            log.error("Error processing JWT token: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        // Se continúa con el resto de la cadena de filtros (y finalmente al Controller).
        filterChain.doFilter(request, response);
    }
}