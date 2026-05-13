package com.learnify.authservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs; // e.g. 86400000 = 24 hours

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, String role) {
        return generateToken(email, role, null);
    }

    // Create a signed JWT with email as subject and role as a custom claim
    public String generateToken(String email, String role, Long userId) {
        var builder = Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs));

        if (userId != null) {
            builder.claim("userId", userId);
        }

        return builder.signWith(getKey()).compact();
    }
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secret.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }
    public Long extractUserId(String token) {
        Object val = extractAllClaims(token).get("userId");
        if (val instanceof Integer) return ((Integer) val).longValue();
        if (val instanceof Long)    return (Long) val;
        return null;
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token); // already verifies signature + expiry
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}
