package com.learnify.enrollment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnrollmentRequest {

    @NotNull(message = "courseId is required")
    private Long courseId;

    // Denormalized fields — sent by client for quick storage
    // (avoids a real-time Feign call to course-service on every enroll)
    private String courseTitle;
}