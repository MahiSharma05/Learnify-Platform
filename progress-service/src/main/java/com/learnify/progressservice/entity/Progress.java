package com.learnify.progressservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "progress",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_student_lesson",
                columnNames = {"studentId", "lessonId"}
        )
)
@Data
@NoArgsConstructor
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key to auth-service user
    @Column(nullable = false)
    private Long studentId;

    private String studentEmail;

    // Foreign key to course-service
    @Column(nullable = false)
    private Long courseId;

    // Foreign key to lesson-service
    @Column(nullable = false)
    private Long lessonId;

    // Total seconds watched for this lesson (accumulates across sessions)
    @Column(nullable = false)
    private Integer watchedSeconds = 0;

    // true = student has fully completed this lesson
    @Column(nullable = false)
    private boolean completed = false;

    // When the lesson was first accessed
    private LocalDateTime firstAccessedAt;

    // When the lesson was marked complete (null if not yet)
    private LocalDateTime completedAt;

    // Last time progress was updated
    @UpdateTimestamp
    private LocalDateTime lastAccessedAt;
}