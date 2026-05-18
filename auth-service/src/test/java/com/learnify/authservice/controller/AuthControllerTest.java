package com.learnify.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnify.authservice.dto.AuthResponse;
import com.learnify.authservice.dto.LoginRequest;
import com.learnify.authservice.dto.RegisterRequest;
import com.learnify.authservice.entity.User;
import com.learnify.authservice.repository.UserRepository;
import com.learnify.authservice.security.JwtUtil;
import com.learnify.authservice.service.AuthService;
import com.learnify.authservice.service.OtpService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private OtpService otpService;

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private Authentication getAuthentication() {

        return new UsernamePasswordAuthenticationToken(
                "mahi@test.com",
                null
        );
    }

    private User getUser() {

        User user = new User();

        user.setId(1L);
        user.setFullName("Mahi Sharma");
        user.setEmail("mahi@test.com");
        user.setRole("STUDENT");
        user.setPasswordHash("encodedPassword");
        user.setBio("Java Developer");
        user.setMobile("9876543210");
        user.setProfilePicUrl("profile.jpg");

        return user;
    }

    private AuthResponse getAuthResponse() {

        return new AuthResponse(
                "jwt-token",
                1L,
                "Mahi Sharma",
                "mahi@test.com",
                "STUDENT",
                "Login successful"
        );
    }

    // =========================================================
    // REGISTER TESTS
    // =========================================================

    @Test
    @DisplayName("Should register successfully")
    void shouldRegisterSuccessfully() throws Exception {

        RegisterRequest request = new RegisterRequest();

        request.setFullName("Mahi Sharma");
        request.setEmail("mahi@test.com");
        request.setPassword("password123");
        request.setRole("STUDENT");

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(getAuthResponse());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("mahi@test.com"));
    }

    @Test
    @DisplayName("Should return 400 for invalid register request")
    void shouldReturn400ForInvalidRegisterRequest() throws Exception {

        RegisterRequest request = new RegisterRequest();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================
    // LOGIN TESTS
    // =========================================================

    @Test
    @DisplayName("Should login successfully")
    void shouldLoginSuccessfully() throws Exception {

        LoginRequest request = new LoginRequest();

        request.setEmail("mahi@test.com");
        request.setPassword("password123");

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(getAuthResponse());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    @DisplayName("Should return 400 for invalid login request")
    void shouldReturn400ForInvalidLoginRequest() throws Exception {

        LoginRequest request = new LoginRequest();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================
    // PROFILE TESTS
    // =========================================================

    @Test
    @DisplayName("Should get profile")
    void shouldGetProfile() throws Exception {

        when(authService.getProfile("mahi@test.com"))
                .thenReturn(getUser());

        mockMvc.perform(get("/api/auth/profile")
                        .with(request -> {
                            request.setUserPrincipal(getAuthentication());
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("mahi@test.com"))
                .andExpect(jsonPath("$.fullName").value("Mahi Sharma"));
    }

    @Test
    @DisplayName("Should update profile")
    void shouldUpdateProfile() throws Exception {

        User user = getUser();

        when(authService.updateProfile(eq("mahi@test.com"), any(User.class)))
                .thenReturn(user);

        mockMvc.perform(put("/api/auth/profile")
                        .with(request -> {
                            request.setUserPrincipal(getAuthentication());
                            return request;
                        })
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Mahi Sharma"));
    }

    // =========================================================
    // PASSWORD TEST
    // =========================================================

    @Test
    @DisplayName("Should change password")
    void shouldChangePassword() throws Exception {

        Map<String, String> body = Map.of(
                "oldPassword", "old123",
                "newPassword", "new123"
        );

        mockMvc.perform(put("/api/auth/password")
                        .with(request -> {
                            request.setUserPrincipal(getAuthentication());
                            return request;
                        })
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password changed successfully"));
    }

    // =========================================================
    // OTP TESTS
    // =========================================================

    @Test
    @DisplayName("Should resend OTP")
    void shouldResendOtp() throws Exception {

        doNothing().when(otpService)
                .generateAndSendOtp("mahi@test.com");

        mockMvc.perform(post("/api/auth/resend-otp")
                        .param("email", "mahi@test.com"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should verify OTP")
    void shouldVerifyOtp() throws Exception {

        Map<String, String> body = Map.of(
                "email", "mahi@test.com",
                "otp", "123456"
        );

        doNothing().when(otpService)
                .verifyOtp("mahi@test.com", "123456");

        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    // =========================================================
    // LOGOUT TEST
    // =========================================================

    @Test
    @DisplayName("Should logout successfully")
    void shouldLogoutSuccessfully() throws Exception {

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully"));
    }

    // =========================================================
    // REFRESH TOKEN TEST
    // =========================================================

    @Test
    @DisplayName("Should refresh token")
    void shouldRefreshToken() throws Exception {

        when(authService.refreshToken("old-token"))
                .thenReturn(getAuthResponse());

        mockMvc.perform(post("/api/auth/refresh")
                        .param("token", "old-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    // =========================================================
    // DELETE ACCOUNT TEST
    // =========================================================

    @Test
    @DisplayName("Should delete account")
    void shouldDeleteAccount() throws Exception {

        mockMvc.perform(delete("/api/auth/delete")
                        .with(request -> {
                            request.setUserPrincipal(getAuthentication());
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(content().string("Account deleted successfully"));
    }

    // =========================================================
    // ADMIN DELETE TEST
    // =========================================================

    @Test
    @DisplayName("Should delete user by admin")
    void shouldDeleteUserByAdmin() throws Exception {

        mockMvc.perform(delete("/api/auth/admin/delete/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted by admin"));
    }

    // =========================================================
    // OAUTH SUCCESS TEST
    // =========================================================

    @Test
    @DisplayName("Should return oauth success response")
    void shouldReturnOauthSuccessResponse() throws Exception {

        User user = getUser();

        when(jwtUtil.extractEmail("oauth-token"))
                .thenReturn("mahi@test.com");

        when(jwtUtil.extractRole("oauth-token"))
                .thenReturn("STUDENT");

        when(userRepository.findByEmail("mahi@test.com"))
                .thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/auth/oauth2/success")
                        .param("token", "oauth-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("mahi@test.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    // =========================================================
    // USER ID TEST
    // =========================================================

    @Test
    @DisplayName("Should return user id by email")
    void shouldReturnUserIdByEmail() throws Exception {

        when(authService.getUserIdByEmail("mahi@test.com"))
                .thenReturn(1L);

        mockMvc.perform(get("/api/auth/user-id")
                        .param("email", "mahi@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    // =========================================================
    // EXCEPTION TEST
    // =========================================================

    @Test
    @DisplayName("Should handle runtime exception")
    void shouldHandleRuntimeException() throws Exception {

        LoginRequest request = new LoginRequest();

        request.setEmail("wrong@test.com");
        request.setPassword("wrong123");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================
//  PROFILE NOT FOUND
// =========================================================

    @Test
    @DisplayName("Should return bad request when profile not found")
    void shouldReturnBadRequestWhenProfileNotFound() throws Exception {

        when(authService.getProfile("mahi@test.com"))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/auth/profile")
                        .with(request -> {
                            request.setUserPrincipal(getAuthentication());
                            return request;
                        }))
                .andExpect(status().isBadRequest());
    }

// =========================================================
//  UPDATE PROFILE FAILURE
// =========================================================

    @Test
    @DisplayName("Should handle update profile failure")
    void shouldHandleUpdateProfileFailure() throws Exception {

        User user = getUser();

        when(authService.updateProfile(anyString(), any(User.class)))
                .thenThrow(new RuntimeException("Update failed"));

        mockMvc.perform(put("/api/auth/profile")
                        .with(request -> {
                            request.setUserPrincipal(getAuthentication());
                            return request;
                        })
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

// =========================================================
// CHANGE PASSWORD FAILURE
// =========================================================

    @Test
    @DisplayName("Should fail when old password incorrect")
    void shouldFailWhenOldPasswordIncorrect() throws Exception {

        Map<String, String> body = Map.of(
                "oldPassword", "wrong",
                "newPassword", "new123"
        );

        doThrow(new RuntimeException("Old password incorrect"))
                .when(authService)
                .changePassword(anyString(), anyString(), anyString());

        mockMvc.perform(put("/api/auth/password")
                        .with(request -> {
                            request.setUserPrincipal(getAuthentication());
                            return request;
                        })
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

// =========================================================
// VERIFY OTP FAILURE
// =========================================================

    @Test
    @DisplayName("Should fail for invalid OTP")
    void shouldFailForInvalidOtp() throws Exception {

        Map<String, String> body = Map.of(
                "email", "mahi@test.com",
                "otp", "999999"
        );

        doThrow(new RuntimeException("Invalid OTP"))
                .when(otpService)
                .verifyOtp(anyString(), anyString());

        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

// =========================================================
// REFRESH TOKEN FAILURE
// =========================================================

    @Test
    @DisplayName("Should fail refresh token")
    void shouldFailRefreshToken() throws Exception {

        when(authService.refreshToken("bad-token"))
                .thenThrow(new RuntimeException("Invalid token"));

        mockMvc.perform(post("/api/auth/refresh")
                        .param("token", "bad-token"))
                .andExpect(status().isBadRequest());
    }

// =========================================================
// DELETE ACCOUNT FAILURE
// =========================================================

    @Test
    @DisplayName("Should fail delete account")
    void shouldFailDeleteAccount() throws Exception {

        doThrow(new RuntimeException("Delete failed"))
                .when(authService)
                .deleteAccount(anyString());

        mockMvc.perform(delete("/api/auth/delete")
                        .with(request -> {
                            request.setUserPrincipal(getAuthentication());
                            return request;
                        }))
                .andExpect(status().isBadRequest());
    }

// =========================================================
// ADMIN DELETE FAILURE
// =========================================================

    @Test
    @DisplayName("Should fail admin delete")
    void shouldFailAdminDelete() throws Exception {

        doThrow(new RuntimeException("User not found"))
                .when(authService)
                .deleteUserByAdmin(1L);

        mockMvc.perform(delete("/api/auth/admin/delete/1"))
                .andExpect(status().isBadRequest());
    }

// =========================================================
// OAUTH USER NOT FOUND
// =========================================================

    @Test
    @DisplayName("Should fail oauth success when user not found")
    void shouldFailOauthSuccessWhenUserNotFound() throws Exception {

        when(jwtUtil.extractEmail("oauth-token"))
                .thenReturn("mahi@test.com");

        when(jwtUtil.extractRole("oauth-token"))
                .thenReturn("STUDENT");

        when(userRepository.findByEmail("mahi@test.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/auth/oauth2/success")
                        .param("token", "oauth-token"))
                .andExpect(status().isBadRequest());
    }
}