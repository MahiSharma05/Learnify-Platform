package com.learnify.notificationservice.controller;

import com.learnify.notificationservice.dto.*;
import com.learnify.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * POST /api/notifications
     * Send a single in-app notification (optionally also email).
     * Called by other microservices internally OR by admin manually.
     * Roles: ADMIN, SYSTEM (internal calls from other services)
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody NotificationRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        // Other services call this without user context — allow all
        // In production: add an internal service API key check here
        return ResponseEntity.status(201)
                .body(notificationService.sendNotification(request));
    }

    /**
     * POST /api/notifications/bulk
     * Send notifications to multiple users or all users.
     * Roles: ADMIN only
     */
    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> sendBulkNotification(
            @Valid @RequestBody BulkNotificationRequest request,
            @RequestHeader("X-User-Role") String role) {

        return ResponseEntity.status(201)
                .body(notificationService.sendBulkNotification(request, role));
    }

    /**
     * GET /api/notifications/my
     * Get all notifications for the logged-in user (newest first).
     * Roles: STUDENT, INSTRUCTOR, ADMIN
     */
    @GetMapping("/my")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        if (userId == null) {
            return ResponseEntity.ok(List.of()); // ✅ empty list
        }

        return ResponseEntity.ok(notificationService.getNotificationsByUser(userId));
    }

    /**
     * GET /api/notifications/my/unread
     * Get only unread notifications for the logged-in user.
     * Roles: STUDENT, INSTRUCTOR, ADMIN
     */
    @GetMapping("/my/unread")
    public ResponseEntity<List<NotificationResponse>> getMyUnread(
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }

    /**
     * GET /api/notifications/unread-count
     * Get unread notification count for badge display.
     * Roles: STUDENT, INSTRUCTOR, ADMIN
     * Returns: { "count": 5 }
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        if (userId == null) {
            return ResponseEntity.ok(Map.of("count", 0L)); // ✅ prevent crash
        }

        return ResponseEntity.ok(
                Map.of("count", notificationService.getUnreadCount(userId)));
    }

    /**
     * GET /api/notifications/{id}
     * Get a single notification by ID.
     * Roles: Owner (STUDENT/INSTRUCTOR), ADMIN
     */
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id")   Long userId,
            @RequestHeader("X-User-Role") String role) {

        return ResponseEntity.ok(
                notificationService.getNotificationById(id, userId, role));
    }

    /**
     * PUT /api/notifications/{id}/read
     * Mark a single notification as read.
     * Roles: Owner only
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(notificationService.markAsRead(id, userId));
    }

    /**
     * PUT /api/notifications/read-all
     * Mark all notifications as read for the logged-in user.
     * Roles: STUDENT, INSTRUCTOR, ADMIN (own notifications)
     * Returns: { "markedRead": 12 }
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(
            @RequestHeader("X-User-Id") Long userId) {

        int count = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("markedRead", count));
    }

    /**
     * DELETE /api/notifications/{id}
     * Delete a single notification.
     * Roles: Owner, ADMIN
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotification(
            @PathVariable Long id,
            @RequestHeader("X-User-Id")   Long userId,
            @RequestHeader("X-User-Role") String role) {

        notificationService.deleteNotification(id, userId, role);
        return ResponseEntity.ok("Notification deleted successfully");
    }

    /**
     * DELETE /api/notifications/user/{userId}
     * Delete ALL notifications for a user.
     * Roles: ADMIN only
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<String> deleteAllForUser(
            @PathVariable Long userId,
            @RequestHeader("X-User-Role") String role) {

        notificationService.deleteAllForUser(userId, role);
        return ResponseEntity.ok("All notifications deleted for user: " + userId);
    }

    /**
     * GET /api/notifications/all
     * Get ALL notifications across the platform.
     * Roles: ADMIN only
     */
    @GetMapping("/all")
    public ResponseEntity<List<NotificationResponse>> getAllNotifications(
            @RequestHeader("X-User-Role") String role) {

        return ResponseEntity.ok(notificationService.getAllNotifications(role));
    }
}