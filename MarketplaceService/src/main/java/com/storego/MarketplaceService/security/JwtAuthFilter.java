package com.storego.MarketplaceService.security;

import com.storego.MarketplaceService.exception.InvalidJwtException;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

            // Permitir acceso a endpoints públicos
            String requestPath = request.getRequestURI();
            if (isPublicEndpoint(requestPath)) {
                filterChain.doFilter(request, response);
                return;
            }

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                log.warn("Token JWT no proporcionado en request: {}", requestPath);
                filterChain.doFilter(request, response);
                return;
            }

            String token = jwtUtil.extractToken(authorizationHeader);
            
            // Validar token
            jwtUtil.validateToken(token);

            // Obtener información del token
            Claims claims = jwtUtil.getClaimsFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            List<String> roles = jwtUtil.getRolesFromToken(token);

            // Convertir roles a SimpleGrantedAuthority
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            // Crear autenticación
            UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            
            // Guardar información adicional en los detalles
            authentication.setDetails(claims);
            
            // Establecer autenticación en el contexto de seguridad
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Usuario autenticado: {}", username);

        } catch (InvalidJwtException ex) {
            log.error("Error de JWT: {}", ex.getMessage());
            request.setAttribute("jwtError", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error en filtro de JWT: {}", ex.getMessage(), ex);
            request.setAttribute("jwtError", "Error procesando token JWT");
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Verifica si un endpoint es público
     */
    private boolean isPublicEndpoint(String requestPath) {
        return requestPath.contains("/swagger-ui") ||
               requestPath.contains("/v3/api-docs") ||
               requestPath.contains("/swagger-resources") ||
               requestPath.contains("/webjars") ||
               requestPath.contains("/actuator") ||
               requestPath.equals("/");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return isPublicEndpoint(request.getRequestURI());
    }
}
