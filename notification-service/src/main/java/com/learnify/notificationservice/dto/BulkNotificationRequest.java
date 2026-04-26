package com.learnify.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkNotificationRequest {

    // List of target user IDs — send to specific users
    // If null/empty, targetAll must be true
    private List<Long> userIds;

    // If true → send to ALL users (admin announcement)
    // If false → send only to userIds list
    private boolean targetAll = false;

    // NotificationType as string — use PLATFORM_ANNOUNCEMENT for broadcasts
    @NotBlank(message = "Notification type is required")
    private String type;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    private Long relatedEntityId;
    private String relatedEntityType;

    // Whether to also send emails for this bulk notification
    private boolean sendEmail = false;
}