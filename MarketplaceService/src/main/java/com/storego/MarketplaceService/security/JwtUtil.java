package com.storego.MarketplaceService.security;

import com.storego.MarketplaceService.exception.InvalidJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret:MySecretKeyForJWTTokenGenerationAndValidationMySecretKeyForJWTTokenGenerationAndValidationMySecretKeyForJWTTokenGenerationAndValidation}")
    private String jwtSecret;

    @Value("${jwt.issuer:storego}")
    private String jwtIssuer;

    /**
     * Obtiene la clave secreta para validar el JWT
     */
    private SecretKey getSigningKey() {
        byte[] decodedKey = Base64.getDecoder().decode(jwtSecret);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA256");
    }

    /**
     * Extrae el token JWT del encabezado Authorization
     */
    public String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new InvalidJwtException("Token JWT no proporcionado o formato inválido");
        }
        return authorizationHeader.substring(7);
    }

    /**
     * Valida el token JWT
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            log.debug("Token JWT validado exitosamente");
            return true;
        } catch (SignatureException ex) {
            log.warn("Firma de JWT inválida: {}", ex.getMessage());
            throw new InvalidJwtException("Firma de JWT inválida", ex);
        } catch (IllegalArgumentException ex) {
            log.warn("Token JWT vacío o nulo: {}", ex.getMessage());
            throw new InvalidJwtException("Token JWT vacío o nulo", ex);
        } catch (Exception ex) {
            log.warn("Error validando JWT: {}", ex.getMessage());
            throw new InvalidJwtException("Error validando JWT: " + ex.getMessage(), ex);
        }
    }

    /**
     * Obtiene los claims del token JWT
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception ex) {
            log.error("Error extrayendo claims del JWT: {}", ex.getMessage());
            throw new InvalidJwtException("Error extrayendo información del JWT", ex);
        }
    }

    /**
     * Obtiene el userId del token JWT
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Object userId = claims.get("userId");
            if (userId instanceof Number) {
                return ((Number) userId).longValue();
            } else if (userId instanceof String) {
                return Long.parseLong((String) userId);
            }
            return null;
        } catch (Exception ex) {
            log.error("Error extrayendo userId del JWT: {}", ex.getMessage());
            throw new InvalidJwtException("Error extrayendo userId del JWT", ex);
        }
    }

    /**
     * Obtiene el username del token JWT
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception ex) {
            log.error("Error extrayendo username del JWT: {}", ex.getMessage());
            throw new InvalidJwtException("Error extrayendo username del JWT", ex);
        }
    }

    /**
     * Obtiene los roles del token JWT
     */
    @SuppressWarnings("unchecked")
    public java.util.List<String> getRolesFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return (java.util.List<String>) claims.get("roles");
        } catch (Exception ex) {
            log.warn("Error extrayendo roles del JWT: {}", ex.getMessage());
            return java.util.List.of();
        }
    }
}
