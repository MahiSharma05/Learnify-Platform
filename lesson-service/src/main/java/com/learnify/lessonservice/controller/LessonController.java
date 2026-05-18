package com.learnify.lessonservice.controller;

import com.learnify.lessonservice.dto.LessonRequest;
import com.learnify.lessonservice.dto.LessonResponse;
import com.learnify.lessonservice.dto.ReorderRequest;
import com.learnify.lessonservice.service.LessonService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    /**
     * POST /api/lessons
     * Add a new lesson to a course.
     * Roles: INSTRUCTOR, ADMIN
     */
    @PostMapping
    public ResponseEntity<LessonResponse> addLesson(
            @Valid @RequestBody LessonRequest request,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        return ResponseEntity.status(201)
                .body(lessonService.addLesson(request, email, role));
    }

    /**
     * GET /api/lessons/course/{courseId}
     * Get all lessons of a course ordered by position.
     * Roles: PUBLIC (students see content after enrollment check in future)
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<LessonResponse>> getLessonsByCourse(
            @PathVariable Long courseId) {

        return ResponseEntity.ok(lessonService.getLessonsByCourse(courseId));
    }

    /**
     * GET /api/lessons/{id}
     * Get a single lesson by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LessonResponse> getLessonById(@PathVariable Long id) {
        return ResponseEntity.ok(lessonService.getLessonById(id));
    }

    /**
     * PUT /api/lessons/{id}
     * Update lesson details.
     * Roles: INSTRUCTOR (owner), ADMIN
     */
    @PutMapping("/{id}")
    public ResponseEntity<LessonResponse> updateLesson(
            @PathVariable Long id,
            @Valid @RequestBody LessonRequest request,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        return ResponseEntity.ok(lessonService.updateLesson(id, request, email, role));
    }

    /**
     * DELETE /api/lessons/{id}
     * Delete a lesson and its resources.
     * Roles: INSTRUCTOR (owner), ADMIN
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLesson(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        lessonService.deleteLesson(id, email, role);
        return ResponseEntity.ok("Lesson deleted successfully");
    }

    /**
     * PUT /api/lessons/reorder
     * Reorder lessons within a course.
     * Body: { courseId: 1, lessonIds: [3, 1, 2] }
     * Roles: INSTRUCTOR, ADMIN
     */
    @PutMapping("/reorder")
    public ResponseEntity<String> reorderLessons(
            @Valid @RequestBody ReorderRequest request,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        lessonService.reorderLessons(request, email, role);
        return ResponseEntity.ok("Lessons reordered successfully");
    }

    /**
     * GET /api/lessons/course/{courseId}/preview
     * Get only preview-flagged lessons (public — no login needed).
     * Used by guests evaluating a course before purchasing.
     */
    @GetMapping("/course/{courseId}/preview")
    public ResponseEntity<List<LessonResponse>> getPreviewLessons(
            @PathVariable Long courseId) {

        return ResponseEntity.ok(lessonService.getPreviewLessons(courseId));
    }
}