package com.learnify.discussionservice.controller;

import com.learnify.discussionservice.dto.ReplyRequest;
import com.learnify.discussionservice.dto.ReplyResponse;
import com.learnify.discussionservice.service.DiscussionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/replies")
public class ReplyController {

    private final DiscussionService discussionService;

    public ReplyController(DiscussionService discussionService) {
        this.discussionService = discussionService;
    }

    /**
     * POST /api/replies
     * Post a reply to a thread.
     * Roles: STUDENT, INSTRUCTOR
     */
    @PostMapping
    public ResponseEntity<ReplyResponse> postReply(
            @Valid @RequestBody ReplyRequest request,
            @RequestHeader("X-User-Id")    Long userId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        return ResponseEntity.status(201)
                .body(discussionService.postReply(request, userId, email, role));
    }

    /**
     * GET /api/replies/thread/{threadId}
     * Get all non-deleted replies for a thread.
     * Accepted answers first, then sorted by upvotes descending.
     * Roles: STUDENT, INSTRUCTOR, ADMIN
     */
    @GetMapping("/thread/{threadId}")
    public ResponseEntity<List<ReplyResponse>> getRepliesByThread(
            @PathVariable Long threadId) {

        return ResponseEntity.ok(discussionService.getRepliesByThread(threadId));
    }

    /**
     * DELETE /api/replies/{id}
     * Soft-delete a reply.
     * Roles: Reply author (own), INSTRUCTOR, ADMIN
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReply(
            @PathVariable Long id,
            @RequestHeader("X-User-Id")    Long userId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        discussionService.deleteReply(id, userId, email, role);
        return ResponseEntity.ok("Reply deleted successfully");
    }

    /**
     * PUT /api/replies/{id}/upvote
     * Upvote a reply (+1).
     * Roles: STUDENT, INSTRUCTOR (anyone logged in)
     */
    @PutMapping("/{id}/upvote")
    public ResponseEntity<ReplyResponse> upvoteReply(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String email) {

        return ResponseEntity.ok(discussionService.upvoteReply(id, email));
    }

    /**
     * PUT /api/replies/{id}/accept
     * Accept a reply as the best answer.
     * Marks thread as resolved.
     * Roles: Thread author, INSTRUCTOR, ADMIN
     */
    @PutMapping("/{id}/accept")
    public ResponseEntity<ReplyResponse> acceptReply(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        return ResponseEntity.ok(discussionService.acceptReply(id, email, role));
    }

    /**
     * PUT /api/replies/{id}/unaccept
     * Remove the accepted/best-answer marker from a reply.
     * Roles: Thread author, INSTRUCTOR, ADMIN
     */
    @PutMapping("/{id}/unaccept")
    public ResponseEntity<ReplyResponse> unacceptReply(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        return ResponseEntity.ok(discussionService.unacceptReply(id, email, role));
    }
}