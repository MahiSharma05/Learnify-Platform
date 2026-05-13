package com.learnify.discussionservice.controller;

import com.learnify.discussionservice.dto.ThreadRequest;
import com.learnify.discussionservice.dto.ThreadResponse;
import com.learnify.discussionservice.service.DiscussionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/threads")
public class ThreadController {

    private final DiscussionService discussionService;

    public ThreadController(DiscussionService discussionService) {
        this.discussionService = discussionService;
    }

    /**
     * POST /api/threads
     * Create a new discussion thread in a course forum.
     * Roles: STUDENT, INSTRUCTOR
     */
    @PostMapping
    public ResponseEntity<ThreadResponse> createThread(
            @Valid @RequestBody ThreadRequest request,
            @RequestHeader("X-User-Id")    Long userId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        return ResponseEntity.status(201)
                .body(discussionService.createThread(request, userId, email, role));
    }

    /**
     * GET /api/threads/course/{courseId}
     * Get all threads for a course — pinned first, then newest.
     * Roles: STUDENT, INSTRUCTOR, ADMIN
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ThreadResponse>> getThreadsByCourse(
            @PathVariable Long courseId) {

        return ResponseEntity.ok(discussionService.getThreadsByCourse(courseId));
    }

    /**
     * GET /api/threads/lesson/{lessonId}
     * Get all threads linked to a specific lesson.
     * Roles: STUDENT, INSTRUCTOR, ADMIN
     */
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<ThreadResponse>> getThreadsByLesson(
            @PathVariable Long lessonId) {

        return ResponseEntity.ok(discussionService.getThreadsByLesson(lessonId));
    }

    /**
     * GET /api/threads/{id}
     * Get a single thread by ID — includes all non-deleted replies.
     * Roles: STUDENT, INSTRUCTOR, ADMIN
     */
    @GetMapping("/{id}")
    public ResponseEntity<ThreadResponse> getThreadById(@PathVariable Long id) {
        return ResponseEntity.ok(discussionService.getThreadById(id));
    }

    /**
     * GET /api/threads/course/{courseId}/search?keyword=java
     * Search threads in a course by keyword in title or body.
     * Roles: STUDENT, INSTRUCTOR, ADMIN
     */
    @GetMapping("/course/{courseId}/search")
    public ResponseEntity<List<ThreadResponse>> searchThreads(
            @PathVariable Long courseId,
            @RequestParam String keyword) {

        return ResponseEntity.ok(discussionService.searchThreads(courseId, keyword));
    }

    /**
     * DELETE /api/threads/{id}
     * Delete a thread and all its replies.
     * Roles: Thread author (own), INSTRUCTOR, ADMIN
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteThread(
            @PathVariable Long id,
            @RequestHeader("X-User-Id")    Long userId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        discussionService.deleteThread(id, userId, email, role);
        return ResponseEntity.ok("Thread deleted successfully");
    }

    /**
     * PUT /api/threads/{id}/pin
     * Pin a thread to the top of the course forum.
     * Roles: INSTRUCTOR, ADMIN
     */
    @PutMapping("/{id}/pin")
    public ResponseEntity<ThreadResponse> pinThread(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        return ResponseEntity.ok(discussionService.pinThread(id, email, role));
    }

    /**
     * PUT /api/threads/{id}/unpin
     * Unpin a thread.
     * Roles: INSTRUCTOR, ADMIN
     */
    @PutMapping("/{id}/unpin")
    public ResponseEntity<ThreadResponse> unpinThread(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        return ResponseEntity.ok(discussionService.unpinThread(id, email, role));
    }

    /**
     * PUT /api/threads/{id}/close
     * Close a thread — no new replies allowed.
     * Roles: INSTRUCTOR, ADMIN
     */
    @PutMapping("/{id}/close")
    public ResponseEntity<ThreadResponse> closeThread(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        return ResponseEntity.ok(discussionService.closeThread(id, email, role));
    }

    /**
     * PUT /api/threads/{id}/reopen
     * Reopen a closed thread.
     * Roles: INSTRUCTOR, ADMIN
     */
    @PutMapping("/{id}/reopen")
    public ResponseEntity<ThreadResponse> reopenThread(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        return ResponseEntity.ok(discussionService.reopenThread(id, email, role));
    }
}