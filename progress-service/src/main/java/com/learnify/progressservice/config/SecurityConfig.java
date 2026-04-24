package com.learnify.progressservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Stateless — sits behind API Gateway.
     * IMPORTANT: /api/certificates/verify/** is intentionally public —
     * anyone (employers, institutions) should be able to verify a certificate
     * without needing to log in. The Gateway is configured to allow this.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                // Public certificate verification — no login required
                                "/api/certificates/verify/**"
                        ).permitAll()
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}