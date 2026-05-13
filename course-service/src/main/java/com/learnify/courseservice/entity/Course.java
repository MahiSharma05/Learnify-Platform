package com.learnify.courseservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import com.learnify.courseservice.enums.ApprovalStatus;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private String category;     // e.g. "Programming", "Design"
    private String level;        // "Beginner", "Intermediate", "Advanced"
    private Double price;        // 0.0 means free
    private String language;     // e.g. "English", "Hindi"
    private String thumbnailUrl;
    private Integer totalDurationMinutes;
    private boolean featured;

    // Auto timestamp
    @CreationTimestamp
    private LocalDateTime createdAt;

    // Enum instead of String
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    // instructorId links to a user in auth-service with role INSTRUCTOR
    @Column(nullable = false)
    private Long instructorId;

    private String instructorName; // Denormalized for quick display

    // false = draft, true = visible in catalog
    @Column(nullable = false)
    private boolean published = false;

}