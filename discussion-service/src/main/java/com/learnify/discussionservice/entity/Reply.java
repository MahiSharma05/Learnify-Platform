package com.learnify.discussionservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "replies")
@Data
@NoArgsConstructor
public class Reply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key to discussion_threads
    @Column(nullable = false)
    private Long threadId;

    // Author of the reply
    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false)
    private String authorEmail;

    // Denormalized for display
    private String authorName;
    private String authorRole; // STUDENT, INSTRUCTOR

    @Column(nullable = false, length = 5000)
    private String body;

    // true = marked as best answer by the thread author or instructor
    @Column(nullable = false)
    private boolean accepted = false;

    // Upvote count — students upvote helpful replies
    @Column(nullable = false)
    private int upvotes = 0;

    // true = reply has been soft-deleted by moderator
    @Column(nullable = false)
    private boolean deleted = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}