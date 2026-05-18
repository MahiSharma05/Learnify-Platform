package com.learnify.courseservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private Long instructorId;
    @NotBlank(message = "Category is required")
    private String category;

    private String level;
    private Double price;
    private String language;
    private String thumbnailUrl;
    private Integer totalDurationMinutes;
}