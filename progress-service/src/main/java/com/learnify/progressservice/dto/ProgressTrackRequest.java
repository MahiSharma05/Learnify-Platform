package com.learnify.progressservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProgressTrackRequest {

    @NotNull(message = "courseId is required")
    private Long courseId;

    @NotNull(message = "lessonId is required")
    private Long lessonId;

    /**
     * Additional seconds watched in this session.
     * Added to any existing watchedSeconds for this lesson.
     * e.g. student watched 120 more seconds → sends 120
     */
    @NotNull(message = "watchedSeconds is required")
    @Min(value = 0, message = "watchedSeconds cannot be negative")
    private Integer watchedSeconds;

    /**
     * Total duration of the lesson in seconds.
     * Used to auto-calculate completion when watchedSeconds >= 90% of totalDuration.
     * Optional — if null, completion must be set explicitly.
     */
    private Integer lessonTotalSeconds;

    /**
     * Explicitly mark lesson as completed.
     * If true, lesson is marked complete regardless of watchedSeconds.
     */
    private boolean markComplete = false;
}