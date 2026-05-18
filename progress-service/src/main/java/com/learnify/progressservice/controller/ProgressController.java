package com.learnify.progressservice.controller;

import com.learnify.progressservice.dto.CourseProgressResponse;
import com.learnify.progressservice.dto.ProgressResponse;
import com.learnify.progressservice.dto.ProgressTrackRequest;
import com.learnify.progressservice.service.ProgressService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    /**
     * POST /api/progress/track
     * Track watch time for a lesson.
     * Called by the frontend every ~30 seconds while video is playing.
     * Roles: STUDENT
     */
    @PostMapping("/track")
    public ResponseEntity<?> trackProgress(
            @Valid @RequestBody ProgressTrackRequest request,
            @RequestHeader("X-User-Id") Long studentId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role) {

        try {
            if (!"STUDENT".equalsIgnoreCase(role)) {
                return ResponseEntity.status(403).body("Invalid role");
            }

            return ResponseEntity.ok(
                    progressService.trackProgress(studentId, email, request)
            );

        } catch (Exception e) {
            e.printStackTrace();   // 👈 THIS LINE

            return ResponseEntity
                    .badRequest()
                    .body("ERROR: " + e.getMessage());
        }
    }

    /**
     * PUT /api/progress/complete
     * Explicitly mark a lesson as complete.
     * Roles: STUDENT
     * Body: { "courseId": 1, "lessonId": 5 }
     */
    @PutMapping("/complete")
    public ResponseEntity<ProgressResponse> markLessonComplete(
            @RequestBody Map<String, Long> body,
            @RequestHeader("X-User-Id")    Long studentId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        if (!"STUDENT".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).build();
        }

        Long courseId = body.get("courseId");
        Long lessonId = body.get("lessonId");

        return ResponseEntity.ok(
                progressService.markLessonComplete(studentId, email, courseId, lessonId));
    }

    /**
     * GET /api/progress/course/{courseId}
     * Get aggregated course progress for the logged-in student.
     * Returns % complete and per-lesson breakdown.
     * Roles: STUDENT
     *
     * Query param: totalLessons (optional) — pass total lesson count from lesson-service
     * If omitted, calculates from tracked lessons only.
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<CourseProgressResponse> getCourseProgress(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") long totalLessons,
            @RequestHeader("X-User-Id") Long studentId) {

        return ResponseEntity.ok(
                progressService.getCourseProgress(studentId, courseId, totalLessons));
    }

    /**
     * GET /api/progress/lesson/{lessonId}
     * Get progress for a specific lesson.
     * Roles: STUDENT
     */
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<ProgressResponse> getLessonProgress(
            @PathVariable Long lessonId,
            @RequestHeader("X-User-Id") Long studentId) {

        return ResponseEntity.ok(progressService.getLessonProgress(studentId, lessonId));
    }

    /**
     * GET /api/progress/my
     * Get all progress records for the logged-in student (all courses).
     * Roles: STUDENT
     */
    @GetMapping("/my")
    public ResponseEntity<List<ProgressResponse>> getAllMyProgress(
            @RequestHeader("X-User-Id") Long studentId) {

        return ResponseEntity.ok(progressService.getAllProgressByStudent(studentId));
    }
}