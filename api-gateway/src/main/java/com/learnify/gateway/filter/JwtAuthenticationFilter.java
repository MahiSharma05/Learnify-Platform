package com.learnify.gateway.filter;

import com.learnify.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    // These paths are open — no JWT required
    private static final List<String> OPEN_ENDPOINTS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/courses/public"   // guests can browse courses
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip JWT check for open endpoints
        if (OPEN_ENDPOINTS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        // Read Authorization header
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        if (!jwtUtil.isTokenValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Forward userId and role as headers so downstream services can use them
        String email = jwtUtil.extractEmail(token);
        String role  = jwtUtil.extractRole(token);

        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(r -> r.headers(headers -> {
                    headers.set("X-User-Email", email);
                    headers.set("X-User-Role", role);
                }))
                .build();

        return chain.filter(modifiedExchange);
    }

    // This filter runs first (lowest order = runs first)
    @Override
    public int getOrder() {
        return -1;
    }
}