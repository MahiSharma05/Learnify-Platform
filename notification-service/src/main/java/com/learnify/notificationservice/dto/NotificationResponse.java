package com.learnify.notificationservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private String type;
    private String title;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private Long relatedEntityId;
    private String relatedEntityType;
    private boolean emailSent;
}