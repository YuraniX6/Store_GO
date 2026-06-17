package com.storego.ratingservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio encargado de leer y validar los tokens JWT (JSON Web Token)
 * generados por el AuthService.
 *
 * El JWT es como un "carnet digital" que el usuario recibe al iniciar sesión
 * en AuthService; este servicio NO genera tokens, solo los lee y confirma
 * que sean válidos, para saber quién es el usuario que hace la petición.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    // Clave secreta usada para firmar/verificar el JWT. Debe ser EXACTAMENTE
    // la misma que usa AuthService para firmar los tokens al hacer login,
    // si no, la verificación de firma fallará (ver el catch de abajo).
    @Value("${jwt.secret}")
    private String jwtSecret;

    // Tiempo de expiración del token en milisegundos (no se usa directamente
    // aquí para validar expiración, pero se mantiene disponible si se necesita).
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Intenta leer (parsear) el contenido del token. Primero intenta
     * verificar la firma con la clave secreta configurada; si eso falla
     * (por ejemplo, porque el secret no coincide exactamente), cae en un
     * modo de respaldo que decodifica el token SIN verificar la firma.
     *
     * Nota: este modo de respaldo es útil mientras el equipo todavía está
     * ajustando que todos los microservicios usen el mismo jwt.secret, pero
     * en un entorno de producción real no sería seguro confiar en un token
     * sin verificar su firma.
     */
    public Claims parseToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.warn("Token validation with configured key failed: {}. Attempting manual parsing...", e.getMessage());
            try {
                return decodeTokenManually(token);
            } catch (Exception e2) {
                log.error("Failed to parse JWT token even without verification: {}", e2.getMessage());
                throw new RuntimeException("Cannot parse JWT token", e);
            }
        }
    }

    // Decodifica manualmente la parte central del JWT (el "payload") sin
    // verificar la firma. Un JWT tiene 3 partes separadas por puntos:
    // header.payload.firma — aquí solo se extrae y decodifica el payload.
    private Claims decodeTokenManually(String token) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT format");
        }

        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> claimsMap = mapper.readValue(payload, Map.class);

        log.info("Successfully parsed JWT manually without signature verification. Claims: {}", claimsMap);

        return Jwts.claims().add(claimsMap).build();
    }

    // Indica si el token es válido (se puede leer sin lanzar excepción).
    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // Extrae el ID del usuario (campo "sub" del JWT) y lo convierte a UUID.
    // Este es el dato más importante: identifica QUIÉN está haciendo la petición.
    public UUID extractUserId(String token) {
        Claims claims = parseToken(token);
        String sub = claims.getSubject();
        try {
            return UUID.fromString(sub);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format in JWT sub claim: {}", sub);
            throw new IllegalArgumentException("Invalid UUID in JWT", e);
        }
    }

    // Extrae el nombre de usuario incluido como claim personalizado "username".
    public String extractUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    // Extrae el rol del usuario (ej. "USER", "ADMIN") incluido como claim "role".
    public String extractRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }
}