package com.learnify.progressservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class CourseProgressResponse {

    private Long studentId;
    private String studentEmail;
    private Long courseId;

    // Total lessons in the course
    private long totalLessons;

    // Number of lessons the student has completed
    private long completedLessons;

    // Percentage completion (0–100)
    private int completionPercent;

    // true if all lessons are completed
    private boolean courseCompleted;

    // Per-lesson breakdown
    private List<ProgressResponse> lessonProgress;
}