package com.learnify.notificationservice.listener;
import com.learnify.notificationservice.dto.NotificationRequest;
import com.learnify.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * EnrollmentEventListener — NEW FILE
 *
 * Consumes ENROLLMENT_CREATED events from RabbitMQ queue:
 *   learnify.enrollment.queue
 *
 * On receipt it:
 *  1. Persists an in-app notification for the student
 *  2. Optionally sends a confirmation email
 *
 * Retry policy: 3 attempts with exponential backoff (configured in application.yml)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EnrollmentEventListener {
    private final NotificationService notificationService;

    /**
     * Triggered when a student successfully enrolls in a course.
     *
     * The incoming payload is the JSON-serialised EnrollmentCreatedEvent
     * from enrollment-service. Spring's Jackson2JsonMessageConverter
     * deserialises it into a Map<String, Object> — this avoids sharing a
     * common event module between services (keeping services independent).
     *
     * @param event deserialized enrollment event payload
     */
    @RabbitListener(queues = "learnify.enrollment.queue")
    public void handleEnrollmentCreated(@Payload Map<String, Object> event) {

        log.info("[EnrollmentListener] Received ENROLLMENT_CREATED event: {}", event);

        try {
            Long   studentId    = toLong(event.get("studentId"));
            String studentEmail = (String) event.get("studentEmail");
            String studentName  = (String) event.get("studentName");
            String courseTitle  = (String) event.get("courseTitle");
            Long   courseId     = toLong(event.get("courseId"));
            Long   enrollmentId = toLong(event.get("enrollmentId"));

            // ── In-app notification ────────────────────────────────────────
            NotificationRequest request = new NotificationRequest();
            request.setUserId(studentId);
            request.setUserEmail(studentEmail);
            request.setType("ENROLLMENT_CONFIRMED");
            request.setTitle("Enrollment Confirmed!");
            request.setMessage(
                    "You have successfully enrolled in \"" + courseTitle + "\". " +
                            "Start learning now and track your progress from your dashboard."
            );
            request.setRelatedEntityId(courseId);
            request.setRelatedEntityType("COURSE");
            request.setSendEmail(studentEmail != null && !studentEmail.isBlank());

            notificationService.sendNotification(request);

            log.info("[EnrollmentListener] Notification sent — student={} course={}",
                    studentId, courseId);

        } catch (Exception e) {
            // Log and swallow — RabbitMQ retry policy handles transient failures.
            // After max-attempts the message is discarded (or sent to DLQ if configured).
            log.error("[EnrollmentListener] Failed to process ENROLLMENT_CREATED event: {}",
                    e.getMessage(), e);
            throw e; // re-throw so Spring AMQP retry policy kicks in
        }
    }

    // ── Helper: safely convert Number or String to Long ──────────────────────
    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.parseLong(value.toString());
    }
}
