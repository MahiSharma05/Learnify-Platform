package com.learnify.authservice.service;

import com.learnify.authservice.dto.AuthResponse;
import com.learnify.authservice.dto.LoginRequest;
import com.learnify.authservice.dto.RegisterRequest;
import com.learnify.authservice.entity.User;

public interface AuthService {

    // Register a new user and return JWT
    AuthResponse register(RegisterRequest request);

    // Login with email+password and return JWT
    AuthResponse login(LoginRequest request);

    // Get user profile by email
    User getProfile(String email);

    // Update user profile
    User updateProfile(String email, User updatedUser);

    // Change password
    void changePassword(String email, String oldPassword, String newPassword);

    // logout
    void logout();

    // Refresh Token
    AuthResponse refreshToken(String token);

    // delete account
    void deleteAccount(String email);

    // delete by Admin
    void deleteUserByAdmin(Long id);

    Long getUserIdByEmail(String email);

    void sendOtp(String email);
}
