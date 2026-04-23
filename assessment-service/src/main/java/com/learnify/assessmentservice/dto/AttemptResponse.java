package com.learnify.assessmentservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class AttemptResponse {

    private Long id;
    private Long quizId;
    private String quizTitle;
    private Long studentId;
    private String studentEmail;

    // Auto-graded results
    private Integer score;          // Percentage score (0-100)
    private Integer marksObtained;
    private Integer totalMarks;
    private boolean passed;
    private Integer passingScore;   // The threshold needed to pass

    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;

    // Student's submitted answers
    private Map<Long, String> answers;

    // Per-question breakdown: questionId → { correct, studentAnswer, correctAnswer }
    private List<QuestionResult> questionResults;

    @Data
    public static class QuestionResult {
        private Long questionId;
        private String questionText;
        private String studentAnswer;
        private String correctAnswer;   // Revealed after submission
        private boolean correct;
        private Integer marksAwarded;
        private Integer marksTotal;
    }
}