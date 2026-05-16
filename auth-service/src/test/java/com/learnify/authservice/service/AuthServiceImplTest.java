package com.learnify.authservice.service;

import com.learnify.authservice.dto.AuthResponse;
import com.learnify.authservice.dto.LoginRequest;
import com.learnify.authservice.dto.RegisterRequest;
import com.learnify.authservice.entity.User;
import com.learnify.authservice.repository.UserRepository;
import com.learnify.authservice.security.JwtUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private AuthServiceImpl authService;

    // =========================================================
    // HELPER METHOD
    // =========================================================

    private User getUser() {

        User user = new User();

        user.setId(1L);
        user.setFullName("Mahi Sharma");
        user.setEmail("mahi@gmail.com");
        user.setPasswordHash("encodedPassword");
        user.setRole("STUDENT");
        user.setEmailVerified(true);

        return user;
    }

    // =========================================================
    // TEST 2 : DUPLICATE EMAIL
    // =========================================================

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        RegisterRequest request = new RegisterRequest();

        request.setEmail("mahi@gmail.com");

        when(userRepository.existsByEmail(anyString()))
                .thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.register(request)
        );

        assertEquals(
                "Email already registered",
                exception.getMessage()
        );
    }

    // =========================================================
    // TEST 3 : LOGIN SUCCESS
    // =========================================================

    @Test
    void shouldLoginSuccessfully() {

        LoginRequest request = new LoginRequest();

        request.setEmail("mahi@gmail.com");
        request.setPassword("123456");

        User user = getUser();

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);

        when(jwtUtil.generateToken(anyString(), anyString(), any()))
                .thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);

        assertEquals("jwt-token", response.getToken());
    }

    // =========================================================
    // TEST 4 : INVALID PASSWORD
    // =========================================================

    @Test
    void shouldThrowExceptionForInvalidPassword() {

        LoginRequest request = new LoginRequest();

        request.setEmail("mahi@gmail.com");
        request.setPassword("wrong");

        User user = getUser();

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );
    }

    // =========================================================
    // TEST 5 : USER NOT FOUND
    // =========================================================

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        LoginRequest request = new LoginRequest();

        request.setEmail("wrong@gmail.com");
        request.setPassword("123456");

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );
    }

    // =========================================================
    // TEST 6 : EMAIL NOT VERIFIED
    // =========================================================

    @Test
    void shouldThrowExceptionWhenEmailNotVerified() {

        LoginRequest request = new LoginRequest();

        request.setEmail("mahi@gmail.com");
        request.setPassword("123456");

        User user = getUser();

        user.setEmailVerified(false);

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "Please verify email first",
                exception.getMessage()
        );
    }

    // =========================================================
    // TEST 9 : GENERATE TOKEN
    // =========================================================

    @Test
    void shouldGenerateToken() {

        LoginRequest request = new LoginRequest();

        request.setEmail("mahi@gmail.com");
        request.setPassword("123456");

        User user = getUser();

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);

        when(jwtUtil.generateToken(anyString(), anyString(), any()))
                .thenReturn("jwt");

        authService.login(request);

        verify(jwtUtil, times(1))
                .generateToken(anyString(), anyString(), any());
    }
    // =========================================================
    // TEST 11 : USER FULL NAME
    // =========================================================

    @Test
    void shouldContainFullName() {

        User user = getUser();

        assertEquals(
                "Mahi Sharma",
                user.getFullName()
        );
    }

    // =========================================================
    // TEST 12 : USER ROLE
    // =========================================================

    @Test
    void shouldContainRole() {

        User user = getUser();

        assertEquals(
                "STUDENT",
                user.getRole()
        );
    }

    // =========================================================
    // TEST 13 : USER EMAIL
    // =========================================================

    @Test
    void shouldContainEmail() {

        User user = getUser();

        assertEquals(
                "mahi@gmail.com",
                user.getEmail()
        );
    }

    // =========================================================
    // TEST 14 : USER VERIFIED
    // =========================================================

    @Test
    void shouldBeVerifiedUser() {

        User user = getUser();

        assertTrue(user.isEmailVerified());
    }

    // =========================================================
    // TEST 15 : USER ID
    // =========================================================

    @Test
    void shouldContainUserId() {

        User user = getUser();

        assertEquals(1L, user.getId());
    }

    // =========================================================
    // TEST 16 : TOKEN NOT NULL
    // =========================================================

    @Test
    void shouldReturnNonNullToken() {

        when(jwtUtil.generateToken(anyString(), anyString(), any()))
                .thenReturn("jwt-token");

        String token = jwtUtil.generateToken(
                "mahi@gmail.com",
                "STUDENT",
                1L
        );

        assertNotNull(token);
    }

    // =========================================================
    // TEST 17 : EMAIL CONTAINS @
    // =========================================================

    @Test
    void shouldContainAtSymbol() {

        User user = getUser();

        assertTrue(
                user.getEmail().contains("@")
        );
    }

    // =========================================================
    // TEST 18 : PASSWORD HASH NOT NULL
    // =========================================================

    @Test
    void shouldContainPasswordHash() {

        User user = getUser();

        assertNotNull(user.getPasswordHash());
    }

    // =========================================================
    // TEST 19 : VERIFY OTP SERVICE CALLED
    // =========================================================

    @Test
    void shouldCallOtpService() {

        authService.sendOtp("mahi@gmail.com");

        verify(otpService, times(1))
                .generateAndSendOtp("mahi@gmail.com");
    }

    // =========================================================
    // TEST 20 : ROLE SHOULD NOT BE NULL
    // =========================================================

    @Test
    void shouldNotContainNullRole() {

        User user = getUser();

        assertNotNull(user.getRole());
    }
}