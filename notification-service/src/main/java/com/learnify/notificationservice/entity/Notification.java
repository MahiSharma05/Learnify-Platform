package com.learnify.notificationservice.entity;

import com.learnify.notificationservice.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notifications",
        indexes = {
                // Optimize reads for "get all notifications for user" queries
                @Index(name = "idx_user_id",      columnList = "userId"),
                @Index(name = "idx_user_is_read", columnList = "userId, isRead")
        }
)
@Data
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Recipient user ID (from auth-service)
    @Column(nullable = false)
    private Long userId;

    private String userEmail;

    // Notification category
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    // Short display title e.g. "Enrollment Confirmed"
    @Column(nullable = false)
    private String title;

    // Full notification message
    @Column(nullable = false, length = 1000)
    private String message;

    // false = new/unread, true = user has seen it
    @Column(nullable = false)
    private boolean isRead = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // When the user opened/read the notification
    private LocalDateTime readAt;

    // Deep-link entity ID (e.g. courseId, quizId, certificateId)
    private Long relatedEntityId;

    // Entity type string for frontend routing
    // e.g. "COURSE", "QUIZ", "CERTIFICATE", "PAYMENT", "THREAD"
    private String relatedEntityType;

    // true = also sent via email; false = in-app only
    @Column(nullable = false)
    private boolean emailSent = false;
}