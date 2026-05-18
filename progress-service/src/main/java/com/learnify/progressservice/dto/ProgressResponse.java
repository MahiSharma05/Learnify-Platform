package com.learnify.progressservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProgressResponse {

    private Long id;
    private Long studentId;
    private String studentEmail;
    private Long courseId;
    private Long lessonId;
    private Integer watchedSeconds;
    private boolean completed;
    private LocalDateTime firstAccessedAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastAccessedAt;
}