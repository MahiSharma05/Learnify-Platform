package com.learnify.enrollment.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EnrollmentResponse {

    private Long id;
    private Long studentId;
    private Long courseId;
    private String studentEmail;
    private String courseTitle;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
    private String status;           // ACTIVE, COMPLETED, CANCELLED
    private Integer progressPercent; // 0–100
    private boolean certificateIssued;
    private String certificateUrl;
}