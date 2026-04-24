package com.learnify.discussionservice.service;

import com.learnify.discussionservice.dto.*;
import com.learnify.discussionservice.entity.DiscussionThread;
import com.learnify.discussionservice.entity.Reply;
import com.learnify.discussionservice.exception.ResourceNotFoundException;
import com.learnify.discussionservice.exception.ThreadClosedException;
import com.learnify.discussionservice.exception.UnauthorizedException;
import com.learnify.discussionservice.repository.ReplyRepository;
import com.learnify.discussionservice.repository.ThreadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscussionServiceImpl implements DiscussionService {

    private final ThreadRepository threadRepository;
    private final ReplyRepository replyRepository;

    public DiscussionServiceImpl(ThreadRepository threadRepository,
                                 ReplyRepository replyRepository) {
        this.threadRepository = threadRepository;
        this.replyRepository  = replyRepository;
    }

    // ── Thread Operations ─────────────────────────────────────────────────────

    @Override
    public ThreadResponse createThread(ThreadRequest request, Long authorId,
                                       String authorEmail, String authorRole) {

        DiscussionThread thread = new DiscussionThread();
        thread.setCourseId(request.getCourseId());
        thread.setLessonId(request.getLessonId());
        thread.setAuthorId(authorId);
        thread.setAuthorEmail(authorEmail);
        thread.setAuthorName(request.getAuthorName() != null
                ? request.getAuthorName() : authorEmail);
        thread.setAuthorRole(authorRole);
        thread.setTitle(request.getTitle());
        thread.setBody(request.getBody());
        thread.setPinned(false);
        thread.setClosed(false);
        thread.setResolved(false);
        thread.setReplyCount(0);

        DiscussionThread saved = threadRepository.save(thread);
        return mapToThreadResponse(saved, List.of());
    }

    @Override
    public List<ThreadResponse> getThreadsByCourse(Long courseId) {
        return threadRepository
                .findByCourseIdOrderByPinnedDescCreatedAtDesc(courseId)
                .stream()
                // In list view, don't include replies (use getThreadById for that)
                .map(thread -> mapToThreadResponse(thread, List.of()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ThreadResponse> getThreadsByLesson(Long lessonId) {
        return threadRepository
                .findByLessonIdOrderByPinnedDescCreatedAtDesc(lessonId)
                .stream()
                .map(thread -> mapToThreadResponse(thread, List.of()))
                .collect(Collectors.toList());
    }

    @Override
    public ThreadResponse getThreadById(Long threadId) {
        DiscussionThread thread = findThreadOrThrow(threadId);

        // Include all non-deleted replies — accepted first, then by upvotes
        List<Reply> replies = replyRepository
                .findByThreadIdAndDeletedFalseOrderByAcceptedDescUpvotesDescCreatedAtAsc(threadId);

        return mapToThreadResponse(thread, replies);
    }

    @Override
    public List<ThreadResponse> searchThreads(Long courseId, String keyword) {
        return threadRepository
                .findByCourseIdAndTitleContainingIgnoreCaseOrBodyContainingIgnoreCase(
                        courseId, keyword, keyword)
                .stream()
                .map(thread -> mapToThreadResponse(thread, List.of()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteThread(Long threadId, Long userId,
                             String userEmail, String userRole) {

        DiscussionThread thread = findThreadOrThrow(threadId);

        // 🔒 Only thread author, INSTRUCTOR, or ADMIN can delete
        if (!thread.getAuthorId().equals(userId) && !isModeratorOrAdmin(userRole)) {
            throw new UnauthorizedException(
                    "You are not authorized to delete this thread");
        }

        // Delete all replies first (cascade)
        replyRepository.deleteByThreadId(threadId);
        threadRepository.delete(thread);
    }

    @Override
    public ThreadResponse pinThread(Long threadId, String userEmail, String userRole) {

        if (!isModeratorOrAdmin(userRole)) {
            throw new UnauthorizedException("Only instructors or admins can pin threads");
        }

        DiscussionThread thread = findThreadOrThrow(threadId);
        thread.setPinned(true);
        return mapToThreadResponse(threadRepository.save(thread), List.of());
    }

    @Override
    public ThreadResponse unpinThread(Long threadId, String userEmail, String userRole) {

        if (!isModeratorOrAdmin(userRole)) {
            throw new UnauthorizedException("Only instructors or admins can unpin threads");
        }

        DiscussionThread thread = findThreadOrThrow(threadId);
        thread.setPinned(false);
        return mapToThreadResponse(threadRepository.save(thread), List.of());
    }

    @Override
    public ThreadResponse closeThread(Long threadId, String userEmail, String userRole) {

        if (!isModeratorOrAdmin(userRole)) {
            throw new UnauthorizedException("Only instructors or admins can close threads");
        }

        DiscussionThread thread = findThreadOrThrow(threadId);
        thread.setClosed(true);
        return mapToThreadResponse(threadRepository.save(thread), List.of());
    }

    @Override
    public ThreadResponse reopenThread(Long threadId, String userEmail, String userRole) {

        if (!isModeratorOrAdmin(userRole)) {
            throw new UnauthorizedException("Only instructors or admins can reopen threads");
        }

        DiscussionThread thread = findThreadOrThrow(threadId);
        thread.setClosed(false);
        return mapToThreadResponse(threadRepository.save(thread), List.of());
    }

    // ── Reply Operations ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public ReplyResponse postReply(ReplyRequest request, Long authorId,
                                   String authorEmail, String authorRole) {

        DiscussionThread thread = findThreadOrThrow(request.getThreadId());

        // 🔒 Cannot reply to a closed thread
        if (thread.isClosed()) {
            throw new ThreadClosedException(
                    "Thread is closed. No new replies are allowed.");
        }

        Reply reply = new Reply();
        reply.setThreadId(request.getThreadId());
        reply.setAuthorId(authorId);
        reply.setAuthorEmail(authorEmail);
        reply.setAuthorName(request.getAuthorName() != null
                ? request.getAuthorName() : authorEmail);
        reply.setAuthorRole(authorRole);
        reply.setBody(request.getBody());
        reply.setAccepted(false);
        reply.setUpvotes(0);
        reply.setDeleted(false);

        Reply saved = replyRepository.save(reply);

        // Update denormalized reply count on thread
        thread.setReplyCount(thread.getReplyCount() + 1);
        threadRepository.save(thread);

        return mapToReplyResponse(saved);
    }

    @Override
    public List<ReplyResponse> getRepliesByThread(Long threadId) {

        // Ensure thread exists
        findThreadOrThrow(threadId);

        return replyRepository
                .findByThreadIdAndDeletedFalseOrderByAcceptedDescUpvotesDescCreatedAtAsc(threadId)
                .stream()
                .map(this::mapToReplyResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteReply(Long replyId, Long userId,
                            String userEmail, String userRole) {

        Reply reply = findReplyOrThrow(replyId);

        // 🔒 Only reply author, INSTRUCTOR, or ADMIN can delete
        if (!reply.getAuthorId().equals(userId) && !isModeratorOrAdmin(userRole)) {
            throw new UnauthorizedException(
                    "You are not authorized to delete this reply");
        }

        // Soft delete — keeps audit trail
        reply.setDeleted(true);
        replyRepository.save(reply);

        // Update thread reply count
        DiscussionThread thread = findThreadOrThrow(reply.getThreadId());
        long activeReplies = replyRepository
                .countByThreadIdAndDeletedFalse(reply.getThreadId());
        thread.setReplyCount((int) activeReplies);
        threadRepository.save(thread);
    }

    @Override
    @Transactional
    public ReplyResponse upvoteReply(Long replyId, String userEmail) {

        Reply reply = findReplyOrThrow(replyId);

        if (reply.isDeleted()) {
            throw new ResourceNotFoundException("Reply has been deleted");
        }

        // Simple increment — in production: track per-user upvotes to prevent duplicates
        reply.setUpvotes(reply.getUpvotes() + 1);
        return mapToReplyResponse(replyRepository.save(reply));
    }

    @Override
    @Transactional
    public ReplyResponse acceptReply(Long replyId, String userEmail, String userRole) {

        Reply reply = findReplyOrThrow(replyId);
        DiscussionThread thread = findThreadOrThrow(reply.getThreadId());

        // 🔒 Only thread author, INSTRUCTOR, or ADMIN can accept a reply
        if (!thread.getAuthorEmail().equals(userEmail) && !isModeratorOrAdmin(userRole)) {
            throw new UnauthorizedException(
                    "Only the thread author, instructors, or admins can accept a reply");
        }

        reply.setAccepted(true);
        Reply saved = replyRepository.save(reply);

        // Mark thread as resolved when a best answer is accepted
        thread.setResolved(true);
        threadRepository.save(thread);

        return mapToReplyResponse(saved);
    }

    @Override
    @Transactional
    public ReplyResponse unacceptReply(Long replyId, String userEmail, String userRole) {

        Reply reply = findReplyOrThrow(replyId);
        DiscussionThread thread = findThreadOrThrow(reply.getThreadId());

        if (!thread.getAuthorEmail().equals(userEmail) && !isModeratorOrAdmin(userRole)) {
            throw new UnauthorizedException(
                    "Only the thread author, instructors, or admins can un-accept a reply");
        }

        reply.setAccepted(false);
        Reply saved = replyRepository.save(reply);

        // Check if any other accepted replies remain
        boolean anyAccepted = replyRepository
                .existsByThreadIdAndAcceptedTrue(reply.getThreadId());
        thread.setResolved(anyAccepted);
        threadRepository.save(thread);

        return mapToReplyResponse(saved);
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private DiscussionThread findThreadOrThrow(Long threadId) {
        return threadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Thread not found with id: " + threadId));
    }

    private Reply findReplyOrThrow(Long replyId) {
        return replyRepository.findById(replyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reply not found with id: " + replyId));
    }

    /**
     * INSTRUCTOR and ADMIN are moderators — they can pin/close/delete any thread.
     */
    private boolean isModeratorOrAdmin(String role) {
        return "INSTRUCTOR".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
    }

    // Map DiscussionThread entity → ThreadResponse DTO
    private ThreadResponse mapToThreadResponse(DiscussionThread thread,
                                               List<Reply> replies) {
        ThreadResponse response = new ThreadResponse();
        response.setId(thread.getId());
        response.setCourseId(thread.getCourseId());
        response.setLessonId(thread.getLessonId());
        response.setAuthorId(thread.getAuthorId());
        response.setAuthorEmail(thread.getAuthorEmail());
        response.setAuthorName(thread.getAuthorName());
        response.setAuthorRole(thread.getAuthorRole());
        response.setTitle(thread.getTitle());
        response.setBody(thread.getBody());
        response.setPinned(thread.isPinned());
        response.setClosed(thread.isClosed());
        response.setResolved(thread.isResolved());
        response.setReplyCount(thread.getReplyCount());
        response.setCreatedAt(thread.getCreatedAt());
        response.setUpdatedAt(thread.getUpdatedAt());

        // Include replies only when explicitly fetched (not in list view)
        if (!replies.isEmpty()) {
            response.setReplies(replies.stream()
                    .map(this::mapToReplyResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    // Map Reply entity → ReplyResponse DTO
    private ReplyResponse mapToReplyResponse(Reply reply) {
        ReplyResponse response = new ReplyResponse();
        response.setId(reply.getId());
        response.setThreadId(reply.getThreadId());
        response.setAuthorId(reply.getAuthorId());
        response.setAuthorEmail(reply.getAuthorEmail());
        response.setAuthorName(reply.getAuthorName());
        response.setAuthorRole(reply.getAuthorRole());
        response.setBody(reply.getBody());
        response.setAccepted(reply.isAccepted());
        response.setUpvotes(reply.getUpvotes());
        response.setDeleted(reply.isDeleted());
        response.setCreatedAt(reply.getCreatedAt());
        response.setUpdatedAt(reply.getUpdatedAt());
        return response;
    }
}