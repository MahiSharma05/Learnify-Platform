package com.learnify.notificationservice.service;

import com.learnify.notificationservice.dto.*;
import com.learnify.notificationservice.entity.Notification;
import com.learnify.notificationservice.enums.NotificationType;
import com.learnify.notificationservice.exception.ResourceNotFoundException;
import com.learnify.notificationservice.exception.UnauthorizedException;
import com.learnify.notificationservice.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${notification.email.from}")
    private String fromEmail;

    @Value("${notification.email.from-name}")
    private String fromName;

    @Value("${notification.max-per-user:100}")
    private int maxPerUser;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   JavaMailSender mailSender) {
        this.notificationRepository = notificationRepository;
        this.mailSender             = mailSender;
    }

    // ── Send Notifications ────────────────────────────────────────────────────

    @Override
    @Transactional
    public NotificationResponse sendNotification(NotificationRequest request) {

        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setUserEmail(request.getUserEmail());
        notification.setType(parseType(request.getType()));
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setRead(false);
        notification.setRelatedEntityId(request.getRelatedEntityId());
        notification.setRelatedEntityType(request.getRelatedEntityType());
        notification.setEmailSent(false);

        Notification saved = notificationRepository.save(notification);

        // Optionally send email
        if (request.isSendEmail()
                && emailEnabled
                && request.getUserEmail() != null
                && !request.getUserEmail().isBlank()) {
            try {
                sendEmailAlert(request.getUserEmail(), request.getTitle(),
                        buildEmailBody(request.getTitle(), request.getMessage()));
                saved.setEmailSent(true);
                notificationRepository.save(saved);
            } catch (Exception e) {
                log.error("[NotificationService] Email send failed for user {}: {}",
                        request.getUserEmail(), e.getMessage());
            }
        }

        // Clean up old notifications if user exceeds maxPerUser
        enforceNotificationLimit(request.getUserId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public Map<String, Object> sendBulkNotification(BulkNotificationRequest request,
                                                    String userRole) {

        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedException("Only admins can send bulk notifications");
        }

        List<Long> targetUserIds = request.getUserIds();

        // targetAll = true means send to every user in the system
        if (request.isTargetAll()) {
            // In production: fetch all userIds from auth-service via Feign
            // For now: use the provided list (admin must supply IDs for targetAll)
            if (targetUserIds == null || targetUserIds.isEmpty()) {
                throw new UnauthorizedException(
                        "targetAll=true requires userIds list (fetch from auth-service)");
            }
        }

        if (targetUserIds == null || targetUserIds.isEmpty()) {
            throw new UnauthorizedException("userIds list cannot be empty");
        }

        int successCount = 0;
        int failCount    = 0;
        List<String> errors = new ArrayList<>();

        for (Long userId : targetUserIds) {
            try {
                NotificationRequest singleRequest = new NotificationRequest();
                singleRequest.setUserId(userId);
                singleRequest.setType(request.getType());
                singleRequest.setTitle(request.getTitle());
                singleRequest.setMessage(request.getMessage());
                singleRequest.setRelatedEntityId(request.getRelatedEntityId());
                singleRequest.setRelatedEntityType(request.getRelatedEntityType());
                singleRequest.setSendEmail(request.isSendEmail());
                sendNotification(singleRequest);
                successCount++;
            } catch (Exception e) {
                failCount++;
                errors.add("Failed for userId=" + userId + ": " + e.getMessage());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalTargeted", targetUserIds.size());
        result.put("successCount",  successCount);
        result.put("failCount",     failCount);
        result.put("errors",        errors);

        return result;
    }

    @Override
    public void sendEmailAlert(String toEmail, String subject, String body) {

        if (!emailEnabled) {
            // Dev mode: log instead of sending
            System.out.println("[NotificationService] [EMAIL DISABLED] To: " + toEmail
                    + " | Subject: " + subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            String htmlBody = buildEmailBody(subject, body);
            helper.setText(htmlBody, true); // true = HTML

            mailSender.send(message);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            System.err.println("[NotificationService] Email failed: " + e.getMessage());
        }
    }

    // ── Read / Retrieve ───────────────────────────────────────────────────────

    @Override
    public List<NotificationResponse> getNotificationsByUser(Long userId) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public NotificationResponse getNotificationById(Long notificationId,
                                                    Long userId, String userRole) {
        Notification notification = findOrThrow(notificationId);

        // 🔒 Users can only see their own notifications (ADMIN can see any)
        if (!notification.getUserId().equals(userId) && !"ADMIN".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedException("Access denied for this notification");
        }

        return mapToResponse(notification);
    }

    @Override
    public List<NotificationResponse> getAllNotifications(String userRole) {
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedException("Only admins can view all notifications");
        }
        return notificationRepository.findAll().stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── Mark Read ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {

        Notification notification = findOrThrow(notificationId);

        // 🔒 Only the recipient can mark their notification as read
        if (!notification.getUserId().equals(userId)) {
            throw new UnauthorizedException(
                    "You can only mark your own notifications as read");
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }

        return mapToResponse(notification);
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsReadForUser(userId);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId, String userRole) {

        Notification notification = findOrThrow(notificationId);

        // 🔒 Only the recipient or ADMIN can delete
        if (!notification.getUserId().equals(userId) && !"ADMIN".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedException(
                    "You are not authorized to delete this notification");
        }

        notificationRepository.delete(notification);
    }

    @Override
    @Transactional
    public void deleteAllForUser(Long userId, String userRole) {
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedException(
                    "Only admins can bulk-delete user notifications");
        }
        notificationRepository.deleteByUserId(userId);
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private Notification findOrThrow(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with id: " + id));
    }

    private NotificationType parseType(String type) {
        try {
            return NotificationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid notification type: " + type);
        }
    }

    /**
     * Auto-cleanup: if user has more than maxPerUser notifications,
     * delete the oldest ones to keep the list manageable.
     */
    private void enforceNotificationLimit(Long userId) {
        long count = notificationRepository.countByUserId(userId);
        if (count > maxPerUser) {
            List<Notification> oldest = notificationRepository
                    .findByUserIdOrderByCreatedAtAsc(userId);
            int toDelete = (int) (count - maxPerUser);
            for (int i = 0; i < toDelete && i < oldest.size(); i++) {
                notificationRepository.delete(oldest.get(i));
            }
        }
    }

    /**
     * Build a simple HTML email body.
     * In production: use Thymeleaf HTML templates for rich emails.
     */
    private String buildEmailBody(String title, String message) {
        return """
            <html>
              <body style="font-family: Arial, sans-serif; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto; background: #f9f9f9;
                            border-radius: 8px; padding: 24px;">
                  <h1 style="color: #2563eb; font-size: 24px;">Learnify</h1>
                  <hr style="border: 1px solid #e5e7eb;" />
                  <h2 style="color: #111827;">%s</h2>
                  <p style="color: #374151; font-size: 16px; line-height: 1.6;">%s</p>
                  <hr style="border: 1px solid #e5e7eb;" />
                  <p style="color: #9ca3af; font-size: 12px;">
                    This is an automated message from Learnify Platform.
                    Please do not reply to this email.
                  </p>
                </div>
              </body>
            </html>
            """.formatted(title, message);
    }

    // Map Notification entity → NotificationResponse DTO
    private NotificationResponse mapToResponse(Notification n) {
        NotificationResponse response = new NotificationResponse();
        response.setId(n.getId());
        response.setUserId(n.getUserId());
        response.setUserEmail(n.getUserEmail());
        response.setType(n.getType().name());
        response.setTitle(n.getTitle());
        response.setMessage(n.getMessage());
        response.setRead(n.isRead());
        response.setCreatedAt(n.getCreatedAt());
        response.setReadAt(n.getReadAt());
        response.setRelatedEntityId(n.getRelatedEntityId());
        response.setRelatedEntityType(n.getRelatedEntityType());
        response.setEmailSent(n.isEmailSent());
        return response;
    }
}