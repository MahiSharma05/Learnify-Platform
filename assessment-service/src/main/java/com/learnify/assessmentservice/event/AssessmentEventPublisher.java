package com.learnify.assessmentservice.event;
import com.learnify.assessmentservice.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * AssessmentEventPublisher — NEW FILE
 *
 * Publishes QUIZ_SUBMITTED events after an attempt is graded and saved.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AssessmentEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    /**
     * Publishes a QuizSubmittedEvent.
     * Called from AssessmentServiceImpl.submitAttempt() after DB save.
     */
    public void publishQuizSubmitted(QuizSubmittedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_QUIZ,
                    event
            );
            log.info("[AssessmentPublisher] Published QUIZ_SUBMITTED — " +
                            "attemptId={} studentId={} score={} passed={}",
                    event.getAttemptId(), event.getStudentId(),
                    event.getScore(), event.isPassed());
        } catch (Exception e) {
            // Never fail quiz grading because notification failed
            log.error("[AssessmentPublisher] Failed to publish QUIZ_SUBMITTED: {}",
                    e.getMessage(), e);
        }
    }
}
