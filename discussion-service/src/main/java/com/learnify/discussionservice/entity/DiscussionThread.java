package com.learnify.discussionservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "discussion_threads")
@Data
@NoArgsConstructor
public class DiscussionThread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key to course-service
    @Column(nullable = false)
    private Long courseId;

    // Optional: link thread to a specific lesson (null = course-level thread)
    private Long lessonId;

    // Author (student or instructor who posted)
    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false)
    private String authorEmail;

    // Denormalized for display (avoids auth-service call on every read)
    private String authorName;

    // Role of the author at time of posting
    private String authorRole; // STUDENT, INSTRUCTOR

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 5000)
    private String body;

    // true = pinned to top of thread list by instructor/admin
    @Column(nullable = false)
    private boolean pinned = false;

    // true = no new replies allowed (closed by instructor/admin)
    @Column(nullable = false)
    private boolean closed = false;

    // Total number of replies (denormalized for performance)
    @Column(nullable = false)
    private int replyCount = 0;

    // true = at least one reply has been accepted as the best answer
    @Column(nullable = false)
    private boolean resolved = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}