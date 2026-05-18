package com.learnify.enrollment.event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.learnify.enrollment.config.RabbitMQConfig;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnrollmentEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    /**
     * Publishes an EnrollmentCreatedEvent to RabbitMQ.
     *
     * @param event the event carrying enrollment details
     */
    public void publishEnrollmentCreated(EnrollmentCreatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_ENROLLMENT,
                    event
            );
            log.info("[EnrollmentPublisher] Published ENROLLMENT_CREATED — " +
                            "enrollmentId={} studentId={} courseId={}",
                    event.getEnrollmentId(), event.getStudentId(), event.getCourseId());
        } catch (Exception e) {
            // Log failure but do NOT throw — enrollment already saved to DB
            // The in-app notification is best-effort
            log.error("[EnrollmentPublisher] Failed to publish ENROLLMENT_CREATED event: {}",
                    e.getMessage(), e);
        }
    }
}
