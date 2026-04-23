package com.learnify.lessonservice.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LessonResponse {

    private Long id;
    private Long courseId;
    private String title;
    private String contentType;
    private String contentUrl;
    private Integer durationMinutes;
    private Integer orderIndex;
    private String description;
    private boolean isPreview;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Attached supplementary files
    private List<ResourceResponse> resources;
}