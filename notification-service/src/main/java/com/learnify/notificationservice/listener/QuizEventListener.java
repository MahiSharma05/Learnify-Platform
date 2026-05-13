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
 * QuizEventListener — NEW FILE
 *
 * Consumes QUIZ_SUBMITTED events from RabbitMQ queue:
 *   learnify.quiz.queue
 *
 * Handles:
 *  - Pass notification: congratulates student with score
 *  - Fail notification: informs student of score + remaining attempts
 *  - Auto-submit notification: timer expired, answers submitted automatically
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class QuizEventListener {
    private final NotificationService notificationService;

    /**
     * Triggered when a student submits a quiz (manual or auto-submit on timer expiry).
     */
    @RabbitListener(queues = "learnify.quiz.queue")
    public void handleQuizSubmitted(@Payload Map<String, Object> event) {

        log.info("[QuizListener] Received QUIZ_SUBMITTED event: {}", event);

        try {
            Long    studentId        = toLong(event.get("studentId"));
            String  studentEmail     = (String)  event.get("studentEmail");
            String  quizTitle        = (String)  event.get("quizTitle");
            Long    courseId         = toLong(event.get("courseId"));
            Long    quizId           = toLong(event.get("quizId"));
            int     score            = toInt(event.get("score"));
            int     passingScore     = toInt(event.get("passingScore"));
            boolean passed           = Boolean.TRUE.equals(event.get("passed"));
            boolean autoSubmitted    = Boolean.TRUE.equals(event.get("autoSubmitted"));
            int     attemptsRemaining = toInt(event.get("attemptsRemaining"));

            NotificationRequest request = new NotificationRequest();
            request.setUserId(studentId);
            request.setUserEmail(studentEmail);
            request.setType("QUIZ_RESULT");
            request.setRelatedEntityId(quizId);
            request.setRelatedEntityType("QUIZ");
            request.setSendEmail(studentEmail != null && !studentEmail.isBlank());

            if (autoSubmitted) {
                // ── Timer expired — auto-submitted ────────────────────────
                request.setTitle("Quiz Auto-Submitted — Time Expired");
                request.setMessage(
                        "Your quiz \"" + quizTitle + "\" was automatically submitted because " +
                                "the time limit expired. Your score: " + score + "/" + passingScore + " " +
                                "(passing score: " + passingScore + "). " +
                                (passed
                                        ? "Congratulations, you passed!"
                                        : "You did not pass this time. " +
                                        buildRetryMessage(attemptsRemaining))
                );

            } else if (passed) {
                // ── Passed ────────────────────────────────────────────────
                request.setTitle("Quiz Passed! 🎉");
                request.setMessage(
                        "Great job! You passed \"" + quizTitle + "\" with a score of " +
                                score + "% (passing score: " + passingScore + "%). " +
                                "Keep up the excellent work!"
                );

            } else {
                // ── Failed ────────────────────────────────────────────────
                request.setTitle("Quiz Result: Not Passed");
                request.setMessage(
                        "You scored " + score + "% on \"" + quizTitle + "\" " +
                                "(passing score: " + passingScore + "%). " +
                                buildRetryMessage(attemptsRemaining)
                );
            }

            notificationService.sendNotification(request);

            log.info("[QuizListener] Notification sent — student={} quiz={} passed={} score={}",
                    studentId, quizId, passed, score);

        } catch (Exception e) {
            log.error("[QuizListener] Failed to process QUIZ_SUBMITTED event: {}",
                    e.getMessage(), e);
            throw e;
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String buildRetryMessage(int attemptsRemaining) {
        if (attemptsRemaining > 0) {
            return "You have " + attemptsRemaining +
                    " attempt" + (attemptsRemaining == 1 ? "" : "s") +
                    " remaining. Review the material and try again!";
        }
        return "You have used all available attempts for this quiz.";
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.parseLong(value.toString());
    }

    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }
}
