package com.learnify.authservice.repository;

import com.learnify.authservice.entity.User;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    // =========================================================
    // HELPER METHOD
    // =========================================================

    private User createUser() {

        User user = new User();

        user.setFullName("Mahi Sharma");

        user.setEmail("mahi@test.com");

        user.setPasswordHash("encodedPassword");

        user.setRole("STUDENT");

        user.setMobile("9876543210");

        user.setBio("Java Developer");

        user.setProfilePicUrl("profile.jpg");

        user.setProvider("local");

        user.setEmailVerified(true);

        return user;
    }

    // =========================================================
    // TEST 1 : SAVE USER
    // =========================================================

    @Test
    @DisplayName("Should save user successfully")
    void shouldSaveUserSuccessfully() {

        User user = createUser();

        User savedUser = userRepository.save(user);

        assertNotNull(savedUser);

        assertNotNull(savedUser.getId());
    }

    // =========================================================
    // TEST 2 : FIND BY EMAIL
    // =========================================================

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {

        User user = createUser();

        userRepository.save(user);

        Optional<User> foundUser =
                userRepository.findByEmail("mahi@test.com");

        assertTrue(foundUser.isPresent());
    }

    // =========================================================
    // TEST 3 : EMAIL EXISTS
    // =========================================================

    @Test
    @DisplayName("Should check email exists")
    void shouldCheckEmailExists() {

        User user = createUser();

        userRepository.save(user);

        boolean exists =
                userRepository.existsByEmail("mahi@test.com");

        assertTrue(exists);
    }

    // =========================================================
    // TEST 4 : FIND USERS BY ROLE
    // =========================================================

    @Test
    @DisplayName("Should find users by role")
    void shouldFindUsersByRole() {

        User user = createUser();

        userRepository.save(user);

        List<User> users =
                userRepository.findAllByRole("STUDENT");

        assertFalse(users.isEmpty());

        assertEquals(1, users.size());
    }

    // =========================================================
    // TEST 5 : DELETE USER
    // =========================================================

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {

        User user = createUser();

        User savedUser =
                userRepository.save(user);

        userRepository.delete(savedUser);

        Optional<User> deletedUser =
                userRepository.findById(savedUser.getId());

        assertFalse(deletedUser.isPresent());
    }

    // =========================================================
    // TEST 6 : INVALID EMAIL
    // =========================================================

    @Test
    @DisplayName("Should return empty for invalid email")
    void shouldReturnEmptyForInvalidEmail() {

        Optional<User> user =
                userRepository.findByEmail("wrong@test.com");

        assertFalse(user.isPresent());
    }

    // =========================================================
    // TEST 7 : SAVE MULTIPLE USERS
    // =========================================================

    @Test
    @DisplayName("Should save multiple users")
    void shouldSaveMultipleUsers() {

        User user1 = createUser();

        User user2 = new User();

        user2.setFullName("Rahul");

        user2.setEmail("rahul@test.com");

        user2.setPasswordHash("encoded");

        user2.setRole("INSTRUCTOR");

        user2.setProvider("local");

        userRepository.save(user1);

        userRepository.save(user2);

        List<User> users =
                userRepository.findAll();

        assertEquals(2, users.size());
    }

    // =========================================================
    // TEST 8 : UPDATE USER
    // =========================================================

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {

        User user = createUser();

        User savedUser =
                userRepository.save(user);

        savedUser.setBio("Spring Boot Developer");

        User updatedUser =
                userRepository.save(savedUser);

        assertEquals(
                "Spring Boot Developer",
                updatedUser.getBio()
        );
    }

    // =========================================================
    // TEST 9 : COUNT USERS
    // =========================================================

    @Test
    @DisplayName("Should count users")
    void shouldCountUsers() {

        User user = createUser();

        userRepository.save(user);

        long count =
                userRepository.count();

        assertEquals(1, count);
    }

    // =========================================================
    // TEST 10 : FIND ALL USERS
    // =========================================================

    @Test
    @DisplayName("Should find all users")
    void shouldFindAllUsers() {

        User user = createUser();

        userRepository.save(user);

        List<User> users =
                userRepository.findAll();

        assertFalse(users.isEmpty());
    }

    // =========================================================
    // TEST 11 : USER ROLE
    // =========================================================

    @Test
    @DisplayName("Should contain student role")
    void shouldContainStudentRole() {

        User user = createUser();

        assertEquals(
                "STUDENT",
                user.getRole()
        );
    }

    // =========================================================
    // TEST 12 : USER FULL NAME
    // =========================================================

    @Test
    @DisplayName("Should contain full name")
    void shouldContainFullName() {

        User user = createUser();

        assertEquals(
                "Mahi Sharma",
                user.getFullName()
        );
    }

    // =========================================================
    // TEST 13 : USER EMAIL
    // =========================================================

    @Test
    @DisplayName("Should contain valid email")
    void shouldContainValidEmail() {

        User user = createUser();

        assertTrue(
                user.getEmail().contains("@")
        );
    }

    // =========================================================
    // TEST 14 : USER PROVIDER
    // =========================================================

    @Test
    @DisplayName("Should contain provider")
    void shouldContainProvider() {

        User user = createUser();

        assertEquals(
                "local",
                user.getProvider()
        );
    }

    // =========================================================
    // TEST 15 : USER VERIFIED
    // =========================================================

    @Test
    @DisplayName("Should be email verified")
    void shouldBeEmailVerified() {

        User user = createUser();

        assertTrue(
                user.isEmailVerified()
        );
    }

    // =========================================================
    // TEST 16 : USER PASSWORD HASH
    // =========================================================

    @Test
    @DisplayName("Should contain password hash")
    void shouldContainPasswordHash() {

        User user = createUser();

        assertNotNull(
                user.getPasswordHash()
        );
    }

    // =========================================================
    // TEST 17 : FIND BY ID
    // =========================================================

    @Test
    @DisplayName("Should find user by id")
    void shouldFindUserById() {

        User user = createUser();

        User savedUser =
                userRepository.save(user);

        Optional<User> foundUser =
                userRepository.findById(savedUser.getId());

        assertTrue(foundUser.isPresent());
    }

    // =========================================================
    // TEST 18 : EMPTY ROLE RESULT
    // =========================================================

    @Test
    @DisplayName("Should return empty role list")
    void shouldReturnEmptyRoleList() {

        List<User> users =
                userRepository.findAllByRole("ADMIN");

        assertTrue(users.isEmpty());
    }

    // =========================================================
    // TEST 19 : UPDATE MOBILE
    // =========================================================

    @Test
    @DisplayName("Should update mobile")
    void shouldUpdateMobile() {

        User user = createUser();

        User savedUser =
                userRepository.save(user);

        savedUser.setMobile("9999999999");

        User updatedUser =
                userRepository.save(savedUser);

        assertEquals(
                "9999999999",
                updatedUser.getMobile()
        );
    }

    // =========================================================
    // TEST 20 : UPDATE PROFILE PIC
    // =========================================================

    @Test
    @DisplayName("Should update profile picture")
    void shouldUpdateProfilePicture() {

        User user = createUser();

        User savedUser =
                userRepository.save(user);

        savedUser.setProfilePicUrl("new.jpg");

        User updatedUser =
                userRepository.save(savedUser);

        assertEquals(
                "new.jpg",
                updatedUser.getProfilePicUrl()
        );
    }
}