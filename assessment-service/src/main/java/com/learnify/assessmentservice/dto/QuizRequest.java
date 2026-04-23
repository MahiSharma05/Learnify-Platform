package com.learnify.assessmentservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuizRequest {

    @NotNull(message = "courseId is required")
    private Long courseId;

    // Optional: link to a specific lesson
    private Long lessonId;

    @NotBlank(message = "Quiz title is required")
    private String title;

    private String description;

    // 0 = no time limit
    @Min(value = 0, message = "Time limit cannot be negative")
    private Integer timeLimitMinutes = 0;

    // 0-100 percentage
    @Min(value = 0,   message = "Passing score cannot be negative")
    @Max(value = 100, message = "Passing score cannot exceed 100")
    private Integer passingScore = 60;

    // 0 = unlimited
    @Min(value = 0, message = "Max attempts cannot be negative")
    private Integer maxAttempts = 3;
}