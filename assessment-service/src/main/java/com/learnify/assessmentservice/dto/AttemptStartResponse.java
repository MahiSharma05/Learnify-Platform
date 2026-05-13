package com.learnify.assessmentservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AttemptStartResponse {

    private Long attemptId;       // Use this ID to submit answers
    private Long quizId;
    private String quizTitle;
    private Integer timeLimitMinutes;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt; // startedAt + timeLimitMinutes (null if no limit)

    // Questions for the student to answer (NO correct answers exposed)
    private List<QuestionResponse> questions;
}