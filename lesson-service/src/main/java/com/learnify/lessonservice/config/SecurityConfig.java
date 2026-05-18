package com.learnify.lessonservice.config;

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
     * This service sits BEHIND the API Gateway.
     * The Gateway already validates the JWT and forwards:
     *   - X-User-Email header
     *   - X-User-Role  header
     *
     * So here we just:
     *   1. Disable CSRF (REST API, stateless)
     *   2. Allow all requests (Gateway handles auth)
     *   3. Use STATELESS session
     *
     * Role checks are done in the service layer using the forwarded headers.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Swagger
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        // All lesson APIs - role checks done in service layer via X-User-Role header
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}