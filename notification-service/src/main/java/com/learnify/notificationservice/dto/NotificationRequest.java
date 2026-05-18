package com.learnify.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    // Target recipient
    @NotNull(message = "userId is required")
    private Long userId;

    private String userEmail;

    // NotificationType enum value as string
    @NotBlank(message = "Notification type is required")
    private String type;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    // Optional deep-link info
    private Long relatedEntityId;
    private String relatedEntityType;

    // Whether to also send an email
    private boolean sendEmail = false;
}