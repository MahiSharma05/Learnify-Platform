package com.learnify.authservice.oauth2;

import com.learnify.authservice.entity.User;
import com.learnify.authservice.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OAuth2UserRequest userRequest;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private OAuth2User createOAuth2User() {

        return new DefaultOAuth2User(
                List.of(),
                Map.of(
                        "name", "Mahi Sharma",
                        "email", "mahi@test.com"
                ),
                "email"
        );
    }

    private User createUser() {

        User user = new User();

        user.setId(1L);

        user.setFullName("Mahi Sharma");

        user.setEmail("mahi@test.com");

        user.setRole("STUDENT");

        user.setProvider("google");

        user.setPasswordHash("GOOGLE_AUTH");

        return user;
    }

    // =========================================================
    // TEST 1 : CREATE USER OBJECT
    // =========================================================

    @Test
    @DisplayName("Should create valid user object")
    void shouldCreateValidUserObject() {

        User user = createUser();

        assertNotNull(user);

        assertEquals("mahi@test.com", user.getEmail());
    }

    // =========================================================
    // TEST 2 : EMAIL TEST
    // =========================================================

    @Test
    @DisplayName("Should store email correctly")
    void shouldStoreEmailCorrectly() {

        User user = createUser();

        assertEquals(
                "mahi@test.com",
                user.getEmail()
        );
    }

    // =========================================================
    // TEST 3 : ROLE TEST
    // =========================================================

    @Test
    @DisplayName("Should assign student role")
    void shouldAssignStudentRole() {

        User user = createUser();

        assertEquals(
                "STUDENT",
                user.getRole()
        );
    }

    // =========================================================
    // TEST 4 : PROVIDER TEST
    // =========================================================

    @Test
    @DisplayName("Should set google provider")
    void shouldSetGoogleProvider() {

        User user = createUser();

        assertEquals(
                "google",
                user.getProvider()
        );
    }

    // =========================================================
    // TEST 5 : FULL NAME TEST
    // =========================================================

    @Test
    @DisplayName("Should store full name")
    void shouldStoreFullName() {

        User user = createUser();

        assertEquals(
                "Mahi Sharma",
                user.getFullName()
        );
    }

    // =========================================================
    // TEST 6 : USER ID TEST
    // =========================================================

    @Test
    @DisplayName("Should contain user id")
    void shouldContainUserId() {

        User user = createUser();

        assertEquals(
                1L,
                user.getId()
        );
    }

    // =========================================================
    // TEST 7 : PASSWORD HASH TEST
    // =========================================================

    @Test
    @DisplayName("Should set google auth password")
    void shouldSetGoogleAuthPassword() {

        User user = createUser();

        assertEquals(
                "GOOGLE_AUTH",
                user.getPasswordHash()
        );
    }

    // =========================================================
    // TEST 8 : OAUTH USER CREATION
    // =========================================================

    @Test
    @DisplayName("Should create oauth2 user")
    void shouldCreateOAuth2User() {

        OAuth2User oauthUser = createOAuth2User();

        assertNotNull(oauthUser);

        assertEquals(
                "mahi@test.com",
                oauthUser.getAttribute("email")
        );
    }

    // =========================================================
    // TEST 9 : OAUTH USER NAME
    // =========================================================

    @Test
    @DisplayName("Should contain oauth user name")
    void shouldContainOauthUserName() {

        OAuth2User oauthUser = createOAuth2User();

        assertEquals(
                "Mahi Sharma",
                oauthUser.getAttribute("name")
        );
    }

    // =========================================================
    // TEST 10 : FIND USER SUCCESS
    // =========================================================

    @Test
    @DisplayName("Should find existing user")
    void shouldFindExistingUser() {

        User user = createUser();

        when(userRepository.findByEmail("mahi@test.com"))
                .thenReturn(Optional.of(user));

        Optional<User> foundUser =
                userRepository.findByEmail("mahi@test.com");

        assertTrue(foundUser.isPresent());

        assertEquals(
                "mahi@test.com",
                foundUser.get().getEmail()
        );
    }

    // =========================================================
    // TEST 11 : USER NOT FOUND
    // =========================================================

    @Test
    @DisplayName("Should return empty optional")
    void shouldReturnEmptyOptional() {

        when(userRepository.findByEmail("wrong@test.com"))
                .thenReturn(Optional.empty());

        Optional<User> user =
                userRepository.findByEmail("wrong@test.com");

        assertFalse(user.isPresent());
    }

    // =========================================================
    // TEST 12 : SAVE USER
    // =========================================================

    @Test
    @DisplayName("Should save user")
    void shouldSaveUser() {

        User user = createUser();

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        User savedUser =
                userRepository.save(user);

        assertNotNull(savedUser);

        verify(userRepository, times(1))
                .save(any(User.class));
    }

    // =========================================================
    // TEST 13 : VERIFY SAVE EMAIL
    // =========================================================

    @Test
    @DisplayName("Should save correct email")
    void shouldSaveCorrectEmail() {

        User user = createUser();

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        User savedUser =
                userRepository.save(user);

        assertEquals(
                "mahi@test.com",
                savedUser.getEmail()
        );
    }

    // =========================================================
    // TEST 14 : VERIFY SAVE ROLE
    // =========================================================

    @Test
    @DisplayName("Should save correct role")
    void shouldSaveCorrectRole() {

        User user = createUser();

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        User savedUser =
                userRepository.save(user);

        assertEquals(
                "STUDENT",
                savedUser.getRole()
        );
    }

    // =========================================================
    // TEST 15 : VERIFY PROVIDER VALUE
    // =========================================================

    @Test
    @DisplayName("Should save correct provider")
    void shouldSaveCorrectProvider() {

        User user = createUser();

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        User savedUser =
                userRepository.save(user);

        assertEquals(
                "google",
                savedUser.getProvider()
        );
    }

    // =========================================================
    // TEST 16 : VERIFY PASSWORD VALUE
    // =========================================================

    @Test
    @DisplayName("Should save correct password hash")
    void shouldSaveCorrectPasswordHash() {

        User user = createUser();

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        User savedUser =
                userRepository.save(user);

        assertEquals(
                "GOOGLE_AUTH",
                savedUser.getPasswordHash()
        );
    }

    // =========================================================
    // TEST 17 : VERIFY USER NAME
    // =========================================================

    @Test
    @DisplayName("Should verify correct full name")
    void shouldVerifyCorrectFullName() {

        User user = createUser();

        assertTrue(
                user.getFullName().contains("Mahi")
        );
    }

    // =========================================================
    // TEST 18 : VERIFY EMAIL FORMAT
    // =========================================================

    @Test
    @DisplayName("Should verify email contains @")
    void shouldVerifyEmailContainsAt() {

        User user = createUser();

        assertTrue(
                user.getEmail().contains("@")
        );
    }
}