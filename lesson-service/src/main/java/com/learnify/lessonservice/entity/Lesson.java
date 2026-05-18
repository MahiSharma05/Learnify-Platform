package com.learnify.lessonservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "lessons")
@Data
@NoArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key reference to course-service (courseId)
    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private String title;

    // Content type: VIDEO, ARTICLE, PDF
    @Column(nullable = false)
    private String contentType;

    // URL to the actual content (video URL, article URL, PDF link)
    private String contentUrl;

    // Duration in minutes
    private Integer durationMinutes;

    // Order position within the course (1-based)
    @Column(nullable = false)
    private Integer orderIndex;

    // Short description / summary for this lesson
    @Column(length = 1000)
    private String description;

    // true = guests can preview this lesson without enrolling
    @Column(nullable = false)
    private boolean isPreview = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}