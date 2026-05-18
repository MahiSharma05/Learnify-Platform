package com.learnify.assessmentservice.dto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * QuizTimerState — NEW FILE
 *
 * Stored in Redis under key: quiz:timer:<attemptId>
 * TTL = quiz timeLimitSeconds + grace period (configured in yml)
 *
 * Purpose:
 *  - Track when the quiz attempt started
 *  - Know the time limit (per quiz configuration)
 *  - Store in-progress answers so we can auto-submit on expiry
 *  - Prevent re-starting an already running attempt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuizTimerState implements Serializable{
    /** The quiz attempt DB ID */
    private Long attemptId;

    /** The quiz being attempted */
    private Long quizId;

    /** The student taking the quiz */
    private Long studentId;

    /** When the attempt was started (UTC) */
    private LocalDateTime startedAt;

    /** Time limit in seconds (from Quiz entity) */
    private int timeLimitSeconds;

    /** In-progress answers: questionId → selectedAnswer */
    private Map<Long, String> currentAnswers;

    /** Whether the timer has already expired and been auto-submitted */
    private boolean autoSubmitted;

    /**
     * Calculates remaining seconds at the point of calling.
     * Returns 0 if time has already expired.
     */
    public long getRemainingSeconds() {
        if (startedAt == null) return 0L;
        long elapsed = java.time.Duration.between(startedAt, LocalDateTime.now()).getSeconds();
        long remaining = timeLimitSeconds - elapsed;
        return Math.max(0L, remaining);
    }

    /**
     * Returns true if the quiz timer has expired.
     */
    @JsonIgnore
    public boolean isExpired() {

        return getRemainingSeconds() <= 0;
    }
}
