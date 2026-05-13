package com.learnify.authservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data               // Generates getters, setters, equals, hashCode, toString
@NoArgsConstructor  // Generates default constructor (required by JPA)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash; // Stored as bcrypt hash, never plain text

    // Role: STUDENT, INSTRUCTOR, ADMIN
    @Column(nullable = false)
    private String role;

    private String mobile;
    private String bio;
    private String profilePicUrl;

    // OAuth provider: "local", "google", "github"
    private String provider;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void setCreatedAt() {
        this.createdAt = LocalDateTime.now();
    }
}