package com.learnify.discussionservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ThreadRequest {

    @NotNull(message = "courseId is required")
    private Long courseId;

    // Optional: link to a specific lesson
    private Long lessonId;

    @NotBlank(message = "Thread title is required")
    private String title;

    @NotBlank(message = "Thread body is required")
    private String body;

    // Author details (populated from Gateway headers in controller)
    private String authorName;
}