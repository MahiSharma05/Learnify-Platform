package com.learnify.notificationservice.service;

import com.learnify.notificationservice.dto.*;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    // ── Send Notifications ────────────────────────────────────────────────────

    // Send a single in-app notification (optionally also email)
    NotificationResponse sendNotification(NotificationRequest request);

    // Send bulk notifications to multiple users or all users (ADMIN)
    Map<String, Object> sendBulkNotification(BulkNotificationRequest request,
                                             String userRole);

    // Send email alert only (no in-app record created)
    void sendEmailAlert(String toEmail, String subject, String body);

    // ── Read / Retrieve ───────────────────────────────────────────────────────

    // Get all notifications for the logged-in user (newest first)
    List<NotificationResponse> getNotificationsByUser(Long userId);

    // Get only unread notifications for a user
    List<NotificationResponse> getUnreadNotifications(Long userId);

    // Get unread notification count (for badge)
    long getUnreadCount(Long userId);

    // Get a single notification by ID
    NotificationResponse getNotificationById(Long notificationId, Long userId,
                                             String userRole);

    // Get ALL notifications across platform (ADMIN)
    List<NotificationResponse> getAllNotifications(String userRole);

    // ── Mark Read ─────────────────────────────────────────────────────────────

    // Mark a single notification as read
    NotificationResponse markAsRead(Long notificationId, Long userId);

    // Mark all notifications as read for a user
    int markAllAsRead(Long userId);

    // ── Delete ────────────────────────────────────────────────────────────────

    // Delete a single notification
    void deleteNotification(Long notificationId, Long userId, String userRole);

    // Delete all notifications for a user (user account cleanup)
    void deleteAllForUser(Long userId, String userRole);
}