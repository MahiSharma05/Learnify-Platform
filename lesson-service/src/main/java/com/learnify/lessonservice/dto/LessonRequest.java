package com.learnify.lessonservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonRequest {

    @NotNull(message = "courseId is required")
    private Long courseId;

    @NotBlank(message = "Title is required")
    private String title;

    // VIDEO, ARTICLE, PDF
    @NotBlank(message = "Content type is required (VIDEO, ARTICLE, PDF)")
    private String contentType;

    private String contentUrl;

    private Integer durationMinutes;

    @NotNull(message = "Order index is required")
    @Min(value = 1, message = "Order index must be at least 1")
    private Integer orderIndex;

    private String description;

    // true = free preview lesson (visible without enrollment)
    private boolean isPreview = false;
}