package com.learnify.assessmentservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class QuestionRequest {

    @NotBlank(message = "Question text is required")
    private String text;

    // MCQ, MULTI, TRUE_FALSE
    @NotBlank(message = "Question type is required (MCQ, MULTI, TRUE_FALSE)")
    private String type;

    // Answer options (at least 2 required for MCQ/MULTI)
    private List<String> options;

    // Correct answer: single value for MCQ/TRUE_FALSE, comma-separated for MULTI
    @NotBlank(message = "Correct answer is required")
    private String correctAnswer;

    @Min(value = 1, message = "Marks must be at least 1")
    private Integer marks = 1;

    @NotNull(message = "Order index is required")
    @Min(value = 1, message = "Order index must be at least 1")
    private Integer orderIndex;
}