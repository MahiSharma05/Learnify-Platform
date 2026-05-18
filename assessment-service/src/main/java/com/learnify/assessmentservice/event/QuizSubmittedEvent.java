package com.learnify.assessmentservice.event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * QuizSubmittedEvent — NEW FILE
 *
 * Published to RabbitMQ exchange "learnify.events"
 * Routing key: "quiz.submitted"
 * Consumed by: notification-service (QuizEventListener)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmittedEvent implements Serializable{
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private Long studentId;
    private String studentEmail;
    private String studentName;
    private Long courseId;
    private String courseTitle;

    /** Score achieved (0–100) */
    private int score;

    /** Passing score threshold */
    private int passingScore;

    /** true = passed, false = failed */
    private boolean passed;

    /** true = auto-submitted by timer expiry */
    private boolean autoSubmitted;

    /** Attempts remaining (max - used) */
    private int attemptsRemaining;

    private LocalDateTime submittedAt;

}
