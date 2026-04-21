package com.learnify.authservice.oauth2;

import com.learnify.authservice.entity.User;
import com.learnify.authservice.repository.UserRepository;
import com.learnify.authservice.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * This handler runs AFTER OAuth login succeeds.
 * Responsibilities:
 * Fetch user from DB
 * Generate JWT
 * Redirect with token
 */
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");

        // User should already exist (saved in CustomOAuth2UserService)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found after OAuth login"));

        // Generate JWT
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        // Redirect to success endpoint with token
        getRedirectStrategy().sendRedirect(
                request,
                response,
                "http://localhost:8081/api/auth/oauth2/success?token=" + token
        );
    }
}