package com.learnify.lessonservice.controller;

import com.learnify.lessonservice.dto.ResourceRequest;
import com.learnify.lessonservice.dto.ResourceResponse;
import com.learnify.lessonservice.service.LessonService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
public class ResourceController {

    private final LessonService lessonService;

    public ResourceController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    /**
     * POST /api/lessons/{lessonId}/resources
     * Attach a downloadable resource to a lesson.
     * Roles: INSTRUCTOR, ADMIN
     */
    @PostMapping("/{lessonId}/resources")
    public ResponseEntity<ResourceResponse> addResource(
            @PathVariable Long lessonId,
            @Valid @RequestBody ResourceRequest request,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        return ResponseEntity.status(201)
                .body(lessonService.addResource(lessonId, request, email, role));
    }

    /**
     * GET /api/lessons/{lessonId}/resources
     * Get all resources attached to a lesson.
     * Public — enrolled students can download.
     */
    @GetMapping("/{lessonId}/resources")
    public ResponseEntity<List<ResourceResponse>> getResources(
            @PathVariable Long lessonId) {

        return ResponseEntity.ok(lessonService.getResourcesByLesson(lessonId));
    }

    /**
     * DELETE /api/lessons/{lessonId}/resources/{resourceId}
     * Remove a resource from a lesson.
     * Roles: INSTRUCTOR, ADMIN
     */
    @DeleteMapping("/{lessonId}/resources/{resourceId}")
    public ResponseEntity<String> removeResource(
            @PathVariable Long lessonId,
            @PathVariable Long resourceId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        lessonService.removeResource(lessonId, resourceId, email, role);
        return ResponseEntity.ok("Resource removed successfully");
    }
}