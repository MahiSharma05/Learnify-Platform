package com.learnify.authservice.service;

import com.learnify.authservice.dto.AuthResponse;
import com.learnify.authservice.dto.LoginRequest;
import com.learnify.authservice.dto.RegisterRequest;
import com.learnify.authservice.entity.User;
import com.learnify.authservice.repository.UserRepository;
import com.learnify.authservice.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public AuthResponse register(RegisterRequest request) {
        // Check if email is already in use
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Build new User entity
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); // bcrypt hash
        user.setRole(request.getRole().toUpperCase());
        user.setProvider("local");

        userRepository.save(user);

        // Generate JWT token for the newly registered user
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(null, null, null, "Registration successful");
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Compare raw password with bcrypt hash
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getEmail(), user.getRole(), "Login successful");
    }

    @Override
    public User getProfile(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User updateProfile(String email, User updatedUser) {
        User existing = getProfile(email);
        existing.setFullName(updatedUser.getFullName());
        existing.setBio(updatedUser.getBio());
        existing.setMobile(updatedUser.getMobile());
        existing.setProfilePicUrl(updatedUser.getProfilePicUrl());
        return userRepository.save(existing);
    }

    @Override
    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = getProfile(email);
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("Old password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void logout() {
        // Nothing to do (JWT is stateless)
    }

    @Override
    public AuthResponse refreshToken(String token) {

        String email = jwtUtil.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newToken = jwtUtil.generateToken(user.getEmail(), user.getRole());

        return new AuthResponse(
                newToken,
                user.getEmail(),
                user.getRole(),
                "Token refreshed successfully"
        );
    }

    @Override
    public void deleteAccount(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(user);
    }

    @Override
    public void deleteUserByAdmin(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(user);
    }
}