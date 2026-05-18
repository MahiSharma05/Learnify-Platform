package com.learnify.assessmentservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "quizzes")
@Data
@NoArgsConstructor
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key to course-service
    @Column(nullable = false)
    private Long courseId;

    // Optional: link quiz to a specific lesson
    private Long lessonId;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    // Time limit in minutes (0 = no limit)
    @Column(nullable = false)
    private Integer timeLimitMinutes = 0;

    // Minimum score (out of 100) to pass
    @Column(nullable = false)
    private Integer passingScore = 60;

    // Maximum number of attempts allowed (0 = unlimited)
    @Column(nullable = false)
    private Integer maxAttempts = 3;

    // false = draft (not visible to students), true = published
    @Column(nullable = false)
    private boolean published = false;

    // Instructor who created this quiz
    private String createdByEmail;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}