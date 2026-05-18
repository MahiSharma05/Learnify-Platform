package com.learnify.courseservice.dto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CourseResponse {
    private Long courseId;   // ✅ IMPORTANT (mapped from entity.id)
    private String title;
    private String description;
    private String category;
    private String level;
    private Double price;
    private String language;
    private String thumbnailUrl;
    private Integer totalDurationMinutes;
    private boolean featured;
    private LocalDateTime createdAt;
    private String approvalStatus;
    private Long instructorId;
    private String instructorName;
    private boolean published;
}
