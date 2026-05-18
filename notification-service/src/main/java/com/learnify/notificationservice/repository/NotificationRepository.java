package com.learnify.notificationservice.repository;

import com.learnify.notificationservice.entity.Notification;
import com.learnify.notificationservice.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // All notifications for a user — newest first
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Only unread notifications for a user
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    // Count unread notifications (for badge count)
    long countByUserIdAndIsReadFalse(Long userId);

    // All notifications of a specific type
    List<Notification> findByType(NotificationType type);

    // Notifications linked to a specific entity
    List<Notification> findByRelatedEntityIdAndRelatedEntityType(Long entityId,
                                                                 String entityType);

    // Mark all unread notifications for a user as read in one query
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP " +
            "WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsReadForUser(@Param("userId") Long userId);

    // Delete all notifications for a user
    void deleteByUserId(Long userId);

    // Count total notifications for a user (for cleanup logic)
    long countByUserId(Long userId);

    // Get oldest notifications for a user (for cleanup when limit exceeded)
    List<Notification> findByUserIdOrderByCreatedAtAsc(Long userId);
}