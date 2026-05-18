package com.learnify.assessmentservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionResponse {

    private Long id;
    private Long quizId;
    private String text;
    private String type;
    private List<String> options;
    private Integer marks;
    private Integer orderIndex;

    // ⚠️ correctAnswer is intentionally excluded from this DTO
    // It is only revealed after attempt submission in AttemptResponse
    // To show correct answers after grading, use AttemptResponse.questionResults
}