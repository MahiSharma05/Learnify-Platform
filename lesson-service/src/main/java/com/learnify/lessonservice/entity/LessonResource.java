package com.learnify.lessonservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_resources")
@Data
@NoArgsConstructor
public class LessonResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key to lesson
    @Column(nullable = false)
    private Long lessonId;

    // Display name e.g. "Chapter 1 Notes.pdf"
    @Column(nullable = false)
    private String name;

    // Download URL (S3 / local)
    @Column(nullable = false)
    private String fileUrl;

    // File type: PDF, SLIDES, CODE, ZIP, OTHER
    private String fileType;

    // File size in KB
    private Long sizeKb;

    @CreationTimestamp
    private LocalDateTime uploadedAt;
}