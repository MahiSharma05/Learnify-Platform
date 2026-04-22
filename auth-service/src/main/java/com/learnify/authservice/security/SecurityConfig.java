package com.learnify.authservice.security;

import com.learnify.authservice.oauth2.CustomOAuth2UserService;
import com.learnify.authservice.oauth2.OAuth2LoginSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF for APIs
                .csrf(csrf -> csrf.disable())

                // ⚠️ IMPORTANT:
                // OAuth2 needs session temporarily (state param)
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth

                        // Public APIs
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/oauth2/**").permitAll()

                        // Student APIs
                        .requestMatchers("/api/auth/profile/**").hasAnyRole("STUDENT", "INSTRUCTOR", "ADMIN")

                        // Instructor APIs (example future)
                        .requestMatchers("/api/instructor/**").hasRole("INSTRUCTOR")

                        // Admin APIs
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Everything else
                        .anyRequest().authenticated()
                )

                // OAuth2 Login Configuration
                .oauth2Login(oauth2 -> oauth2

                        // THIS WAS MISSING (VERY IMPORTANT)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )

                        // Your success handler
                        .successHandler(oAuth2LoginSuccessHandler)
                );

        // Add JWT filter before Spring Security authentication filter
        http.addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Password Encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}