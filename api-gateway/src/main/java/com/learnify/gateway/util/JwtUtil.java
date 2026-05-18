package com.learnify.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    // Build signing key from the configured secret string
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Extract all claims from a JWT token
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Check if the token is valid (not expired, correct signature)
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Get the user's email (subject) from the token
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Get the user's role from the token
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // NEW: extract userId from JWT claim
    public Long extractUserId(String token) {
        Object userIdObj = extractAllClaims(token).get("userId");
        if (userIdObj instanceof Integer) return ((Integer) userIdObj).longValue();
        if (userIdObj instanceof Long)    return (Long) userIdObj;
        return null;
    }
}
