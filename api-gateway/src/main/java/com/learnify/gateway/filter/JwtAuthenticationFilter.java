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

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        // ================= PUBLIC ENDPOINTS =================
        // Allow ALL GET requests for course browsing (students/guests)
        // This includes:
        // /api/courses
        // /api/courses/search
        // /api/courses/filter
        // /api/courses/featured
        if (method.equals("GET") && path.startsWith("/api/courses")) {
            return chain.filter(exchange);
        }

        // Allow authentication APIs without token
        if (path.startsWith("/api/auth")) {
            return chain.filter(exchange);
        }

        // ================= JWT VALIDATION =================

        // Read Authorization header
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        // ❌ If no token OR invalid format
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Extract token (remove "Bearer ")
        String token = authHeader.substring(7);

        // ❌ Validate token
        if (!jwtUtil.isTokenValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // ================= EXTRACT USER INFO =================

        String email = jwtUtil.extractEmail(token);
        String role  = jwtUtil.extractRole(token);

        // 🔥 DEBUG (can remove later)
        System.out.println("Gateway EMAIL: " + email);
        System.out.println("Gateway ROLE: " + role);
        System.out.println("Request PATH: " + path);
        System.out.println("Request METHOD: " + method);

        // ================= ROLE-BASED ACCESS CONTROL =================

        // 🔒 ADMIN ONLY APIs
        // Approve / Reject / View Pending courses
        if (path.contains("/approve") || path.contains("/reject") || path.contains("/pending")) {
            if (!isAdmin(role)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        }

        // 🔒 CREATE COURSE → only INSTRUCTOR
        if (method.equals("POST") && path.startsWith("/api/courses")) {
            if (!isInstructor(role)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        }

        // 🔒 UPDATE / DELETE → INSTRUCTOR or ADMIN
        if ((method.equals("PUT") || method.equals("DELETE")) &&
                path.startsWith("/api/courses")) {

            if (!isInstructor(role) && !isAdmin(role)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        }

        // ================= HEADER FORWARDING =================

        // Pass user info to downstream services (course-service)
        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(builder -> builder.headers(headers -> {
                    headers.set("X-User-Email", email);
                    headers.set("X-User-Role", role);
                }))
                .build();

        return chain.filter(modifiedExchange);
    }

    // ================= HELPER METHODS =================

    // Check ADMIN role
    private boolean isAdmin(String role) {
        return "ADMIN".equalsIgnoreCase(role);
    }

    // Check INSTRUCTOR role
    private boolean isInstructor(String role) {
        return "INSTRUCTOR".equalsIgnoreCase(role);
    }

    // Run this filter first
    @Override
    public int getOrder() {
        return -1;
    }
}