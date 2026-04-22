package com.learnify.authservice.repository;

import com.learnify.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email (used during login)
    Optional<User> findByEmail(String email);

    // Check if email already registered
    boolean existsByEmail(String email);

    // Admin use: get all users by role
    List<User> findAllByRole(String role);
}
