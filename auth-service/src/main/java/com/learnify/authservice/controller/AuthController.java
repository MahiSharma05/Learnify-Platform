package com.learnify.authservice.controller;

import com.learnify.authservice.dto.AuthResponse;
import com.learnify.authservice.dto.LoginRequest;
import com.learnify.authservice.dto.RegisterRequest;
import com.learnify.authservice.entity.User;
import com.learnify.authservice.repository.UserRepository;
import com.learnify.authservice.security.JwtUtil;
import com.learnify.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);

        return ResponseEntity.status(201).body(response);
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    // Get Profile (JWT based)
    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(Authentication authentication) {
        String email = authentication.getName();
        User saved = authService.getProfile(email);
        saved.setPasswordHash(null);
        return ResponseEntity.ok(saved);
    }

    // Update Profile
    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(
            Authentication authentication,
            @RequestBody User user) {

        String email = authentication.getName();
        return ResponseEntity.ok(authService.updateProfile(email, user));
    }

    // PUT /api/auth/password
    @PutMapping("/password")
    public ResponseEntity<String> changePassword(
            Authentication authentication,
            @RequestBody Map<String, String> body) {

        String email = (String) authentication.getPrincipal();
        authService.changePassword(
                email,
                body.get("oldPassword"),
                body.get("newPassword")
        );
        return ResponseEntity.ok("Password changed successfully");
    }

    // Logout
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Logged out successfully");
    }

    // Refresh token
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestParam("token") String token) {

        return ResponseEntity.ok(authService.refreshToken(token));
    }

    // Delete Account
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteAccount(Authentication authentication) {

        String email = authentication.getName();

        authService.deleteAccount(email);

        return ResponseEntity.ok("Account deleted successfully");
    }

    // Delete Account by Admin
    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<String> deleteUserByAdmin(@PathVariable Long id) {
        authService.deleteUserByAdmin(id);
        return ResponseEntity.ok("User deleted by admin");
    }

    // GET /api/auth/oauth2/success?token=xxxxx
    // (Browser lands here after Google login)
    @GetMapping("/oauth2/success")
    public ResponseEntity<AuthResponse> oauthSuccess(@RequestParam("token") String token) {

        // Extract user details from token
        String email = jwtUtil.extractEmail(token);
        String role  = jwtUtil.extractRole(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                new AuthResponse(
                        token,
                        user.getId(),              // ✅ FIX
                        user.getFullName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getProfilePicUrl()
                )
        );
    }
    @GetMapping("/user-id")
    public ResponseEntity<Long> getUserIdByEmail(@RequestParam String email) {
        return ResponseEntity.ok(authService.getUserIdByEmail(email));
    }
}