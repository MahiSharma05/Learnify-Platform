package com.learnify.discussionservice.service;

import com.learnify.discussionservice.dto.*;

import java.util.List;

public interface DiscussionService {

    // ── Thread Operations ─────────────────────────────────────────────────────

    // Create a new thread in a course forum
    ThreadResponse createThread(ThreadRequest request, Long authorId,
                                String authorEmail, String authorRole);

    // Get all threads for a course (pinned first, then newest)
    List<ThreadResponse> getThreadsByCourse(Long courseId);

    // Get all threads linked to a specific lesson
    List<ThreadResponse> getThreadsByLesson(Long lessonId);

    // Get a single thread by ID with all its replies
    ThreadResponse getThreadById(Long threadId);

    // Search threads in a course by keyword
    List<ThreadResponse> searchThreads(Long courseId, String keyword);

    // Delete a thread and all its replies (author or ADMIN/INSTRUCTOR)
    void deleteThread(Long threadId, Long userId, String userEmail, String userRole);

    // Pin a thread (INSTRUCTOR, ADMIN)
    ThreadResponse pinThread(Long threadId, String userEmail, String userRole);

    // Unpin a thread (INSTRUCTOR, ADMIN)
    ThreadResponse unpinThread(Long threadId, String userEmail, String userRole);

    // Close a thread — no more replies allowed (INSTRUCTOR, ADMIN)
    ThreadResponse closeThread(Long threadId, String userEmail, String userRole);

    // Reopen a closed thread (INSTRUCTOR, ADMIN)
    ThreadResponse reopenThread(Long threadId, String userEmail, String userRole);

    // ── Reply Operations ──────────────────────────────────────────────────────

    // Post a reply to a thread
    ReplyResponse postReply(ReplyRequest request, Long authorId,
                            String authorEmail, String authorRole);

    // Get all replies for a thread (non-deleted, accepted first then by upvotes)
    List<ReplyResponse> getRepliesByThread(Long threadId);

    // Delete a reply (author own, or INSTRUCTOR/ADMIN)
    void deleteReply(Long replyId, Long userId, String userEmail, String userRole);

    // Upvote a reply (+1 upvote per call)
    ReplyResponse upvoteReply(Long replyId, String userEmail);

    // Accept a reply as the best answer (thread author, INSTRUCTOR, or ADMIN)
    ReplyResponse acceptReply(Long replyId, String userEmail, String userRole);

    // Un-accept a previously accepted reply
    ReplyResponse unacceptReply(Long replyId, String userEmail, String userRole);
}