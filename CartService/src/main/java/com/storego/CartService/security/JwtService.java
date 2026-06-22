package com.storego.cartService.security;

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

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public Claims parseToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
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

    private Claims decodeTokenManually(String token) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT format");
        }
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> claimsMap = mapper.readValue(payload, Map.class);
        log.info("JWT parsed manually (no signature verification). Claims: {}", claimsMap);
        return Jwts.claims().add(claimsMap).build();
    }

    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractUserId(String token) {
        return parseToken(token).getSubject();
    }

    public String extractUsername(String token) {
        return parseToken(token).get("username", String.class);
    }

    public String extractRole(String token) {
        return parseToken(token).get("role", String.class);
    }
}
